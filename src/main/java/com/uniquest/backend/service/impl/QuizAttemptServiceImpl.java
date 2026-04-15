package com.uniquest.backend.service.impl;

import com.uniquest.backend.dto.quiz.*;
import com.uniquest.backend.enums.AttemptStatus;
import com.uniquest.backend.enums.PerformanceLevel;
import com.uniquest.backend.exception.BadRequestException;
import com.uniquest.backend.exception.ConflictException;
import com.uniquest.backend.exception.ResourceNotFoundException;
import com.uniquest.backend.model.Question;
import com.uniquest.backend.model.QuizAttempt;
import com.uniquest.backend.model.User;
import com.uniquest.backend.model.embedded.Answer;
import com.uniquest.backend.model.embedded.QuestionSnapshot;
import com.uniquest.backend.repository.QuizAttemptRepository;
import com.uniquest.backend.repository.SubjectRepository;
import com.uniquest.backend.repository.UserRepository;
import com.uniquest.backend.service.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate; // used for $sample aggregation

    @Value("${app.quiz.default-question-limit:20}")
    private int questionLimit;

    // ── Start attempt ────────────────────────────────────────────────────────

    /**
     * Creates a new quiz attempt for the authenticated student.
     *
     * Questions are selected using MongoDB's $sample operator which performs
     * a true random selection at the database level — faster and more uniform
     * than loading all questions and shuffling in Java.
     *
     * Snapshots are taken immediately so the result is immutable even if an
     * admin edits or deletes the original question documents later.
     *
     * @param username  JWT subject (username), resolved to User internally
     */
    @Override
    public StartAttemptResponse startAttempt(String username, StartAttemptRequest request) {
        User user = resolveUser(username);

        // Validate subject exists and is not deleted
        subjectRepository.findById(request.getSubjectId())
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.getSubjectId()));

        // Random question selection via MongoDB $sample — avoids loading entire collection
        List<Question> questions = sampleQuestions(
                request.getSubjectId(), request.getCategory().name(), questionLimit);

        if (questions.isEmpty()) {
            throw new BadRequestException(
                    "No questions available for this subject and type. Please contact an admin.");
        }

        // Snapshot every question — this is the source of truth for scoring and review
        List<QuestionSnapshot> snapshots = questions.stream()
                .map(this::toSnapshot)
                .toList();

        // Persist the attempt in IN_PROGRESS state
        QuizAttempt attempt = QuizAttempt.builder()
                .userId(user.getId())
                .subjectId(request.getSubjectId())
                .category(request.getCategory())
                .status(AttemptStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .totalQuestions(snapshots.size())
                .questionSnapshots(snapshots)
                .answers(new ArrayList<>())
                .build();

        attempt = attemptRepository.save(attempt);

        log.info("Attempt started: id={} user={} subject={} questions={}",
                attempt.getId(), username, request.getSubjectId(), snapshots.size());

        // Return questions to the student — correctAnswer is NOT included
        List<QuestionDTO> questionDTOs = snapshots.stream()
                .map(s -> QuestionDTO.builder()
                        .id(s.getQuestionId())
                        .questionText(s.getQuestionText())
                        .options(s.getOptions())
                        .format(s.getFormat())
                        .build())
                .toList();

        return StartAttemptResponse.builder()
                .attemptId(attempt.getId())
                .questions(questionDTOs)
                .build();
    }

    // ── Submit attempt ───────────────────────────────────────────────────────

    /**
     * Scores the attempt and finalizes it as SUBMITTED.
     *
     * Scoring reads exclusively from the stored QuestionSnapshot — never from
     * the live questions collection. This guarantees historically stable results.
     *
     * Answers for questions the student skipped are recorded as incorrect with
     * selectedAnswer=null. This means total questions always equals the number
     * of questions served, and wrongCount = total - correctCount.
     *
     * @param username  JWT subject
     */
    @Override
    public ResultResponse submitAttempt(String username, String attemptId,
                                        SubmitAttemptRequest request) {
        User user = resolveUser(username);

        // Ownership-aware lookup — prevents accessing another user's attempt
        QuizAttempt attempt = attemptRepository.findByIdAndUserId(attemptId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attempt not found or does not belong to you"));

        // Guard: prevent double submission
        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new ConflictException("This attempt has already been submitted");
        }

        // Build a map of submitted answers: questionId -> selectedAnswer
        Map<String, String> submittedAnswers = request.getAnswers().stream()
                .collect(Collectors.toMap(
                        AnswerRequest::getQuestionId,
                        AnswerRequest::getSelectedAnswer,
                        (existing, duplicate) -> existing  // keep first on duplicate questionId
                ));

        // Score each question from the snapshot (not from the DB)
        List<Answer> answers = new ArrayList<>();
        int correctCount = 0;

        for (QuestionSnapshot snapshot : attempt.getQuestionSnapshots()) {
            String selected = submittedAnswers.get(snapshot.getQuestionId());
            boolean isCorrect = selected != null
                    && selected.equalsIgnoreCase(snapshot.getCorrectAnswer());

            if (isCorrect) correctCount++;

            answers.add(Answer.builder()
                    .questionId(snapshot.getQuestionId())
                    .selectedAnswer(selected)          // null = skipped
                    .correctAnswer(snapshot.getCorrectAnswer())
                    .correct(isCorrect)
                    .build());
        }

        // Compute summary stats
        int total        = attempt.getQuestionSnapshots().size();
        int wrongCount   = total - correctCount;
        double percentage = total > 0 ? Math.round((correctCount * 100.0 / total) * 10.0) / 10.0 : 0.0;
        Instant finishedAt = Instant.now();
        long durationSeconds = Duration.between(attempt.getStartedAt(), finishedAt).getSeconds();

        // Finalize the attempt document
        attempt.setAnswers(answers);
        attempt.setCorrectCount(correctCount);
        attempt.setWrongCount(wrongCount);
        attempt.setPercentage(percentage);
        attempt.setFinishedAt(finishedAt);
        attempt.setDurationSeconds(durationSeconds);
        attempt.setStatus(AttemptStatus.SUBMITTED);

        attemptRepository.save(attempt);

        log.info("Attempt submitted: id={} user={} score={}/{} ({}%)",
                attemptId, username, correctCount, total, percentage);

        return buildResultResponse(attempt);
    }

    // ── Get result ───────────────────────────────────────────────────────────

    /**
     * Retrieves a previously submitted result.
     * All data is read from the stored attempt document — zero secondary queries.
     */
    @Override
    public ResultResponse getResult(String username, String attemptId) {
        User user = resolveUser(username);

        QuizAttempt attempt = attemptRepository.findByIdAndUserId(attemptId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attempt not found or does not belong to you"));

        if (attempt.getStatus() != AttemptStatus.SUBMITTED) {
            throw new BadRequestException("Attempt has not been submitted yet");
        }

        return buildResultResponse(attempt);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Builds the full ResultResponse from a finalized QuizAttempt.
     * Per-question review is assembled by joining answers with their snapshots.
     * This is O(n) in memory — no additional DB calls.
     */
    private ResultResponse buildResultResponse(QuizAttempt attempt) {
        // Index snapshots by questionId for O(1) lookup
        Map<String, QuestionSnapshot> snapshotMap = attempt.getQuestionSnapshots().stream()
                .collect(Collectors.toMap(QuestionSnapshot::getQuestionId, s -> s));

        List<QuestionReviewDTO> review = attempt.getAnswers().stream()
                .map(answer -> {
                    QuestionSnapshot snapshot = snapshotMap.get(answer.getQuestionId());
                    return QuestionReviewDTO.builder()
                            .questionText(snapshot.getQuestionText())
                            .options(snapshot.getOptions())
                            .selectedAnswer(answer.getSelectedAnswer())
                            .correctAnswer(answer.getCorrectAnswer())
                            .correct(answer.isCorrect())
                            .explanation(snapshot.getExplanation())
                            .build();
                })
                .toList();

        PerformanceLevel performance = PerformanceLevel.fromPercentage(attempt.getPercentage());

        return ResultResponse.builder()
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount(attempt.getCorrectCount())
                .wrongCount(attempt.getWrongCount())
                .percentage(attempt.getPercentage())
                .durationSeconds(attempt.getDurationSeconds())
                .performance(performance)
                .review(review)
                .build();
    }

    /**
     * Uses MongoDB's $sample aggregation stage for random, index-efficient
     * question selection. Avoids loading the full collection into Java memory.
     *
     * Pipeline: MATCH(subjectId, category) -> $sample(limit)
     */
    private List<Question> sampleQuestions(String subjectId, String category, int limit) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("subjectId").is(subjectId)
                                .and("category").is(category)
                                .and("deleted").ne(true)
                ),
                Aggregation.sample(limit)
        );

        AggregationResults<Question> results =
                mongoTemplate.aggregate(aggregation, "questions", Question.class);

        return results.getMappedResults();
    }

    /**
     * Takes a full immutable snapshot of a question at the moment the attempt starts.
     * Options are converted to a Map<String, String> for consistent JSON output.
     *
     * Only the keys relevant to the format are included:
     *   MCQ        → A, B, C, D
     *   TRUE_FALSE → A, B
     */
    private QuestionSnapshot toSnapshot(Question question) {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("A", question.getOptions().getA());
        options.put("B", question.getOptions().getB());

        if (question.getFormat() == com.uniquest.backend.enums.QuestionFormat.MCQ) {
            options.put("C", question.getOptions().getC());
            options.put("D", question.getOptions().getD());
        }

        return QuestionSnapshot.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .options(options)
                .format(question.getFormat())
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }

    /** Resolves a JWT username to a User document. */
    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
