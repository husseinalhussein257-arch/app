package com.uniquest.backend.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.uniquest.backend.dto.admin.CreateQuestionRequest;
import com.uniquest.backend.dto.admin.QuestionResponse;
import com.uniquest.backend.dto.admin.UploadResponse;
import com.uniquest.backend.enums.QuestionCategory;
import com.uniquest.backend.enums.QuestionFormat;
import com.uniquest.backend.exception.BadRequestException;
import com.uniquest.backend.exception.ResourceNotFoundException;
import com.uniquest.backend.model.Question;
import com.uniquest.backend.repository.QuestionRepository;
import com.uniquest.backend.repository.SubjectRepository;
import com.uniquest.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV / Excel column order (1-indexed, header row excluded):
 * 1: category      (IMPORTANT or EXAM)
 * 2: format        (MCQ or TRUE_FALSE)
 * 3: questionText
 * 4: A             (option A text)
 * 5: B
 * 6: C             (required for MCQ; leave empty for TRUE_FALSE)
 * 7: D             (required for MCQ; leave empty for TRUE_FALSE)
 * 8: correctAnswer (A/B/C/D for MCQ; A/B for TRUE_FALSE)
 * 9: explanation   (optional)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final MongoTemplate mongoTemplate;

    // ── Manual create ────────────────────────────────────────────────────────

    @Override
    public void createQuestion(CreateQuestionRequest request) {
        if (!subjectRepository.existsByIdAndDeletedFalse(request.getSubjectId())) {
            throw new ResourceNotFoundException("Subject", request.getSubjectId());
        }

        validateOptions(request.getOptions(), request.getFormat(), -1, new ArrayList<>());
        validateCorrectAnswer(request.getCorrectAnswer(), request.getFormat(), -1, new ArrayList<>());

        Question question = buildQuestion(
                request.getSubjectId(),
                request.getCategory(),
                request.getFormat(),
                request.getQuestionText(),
                request.getOptions().get("A"),
                request.getOptions().get("B"),
                request.getFormat() == QuestionFormat.MCQ ? request.getOptions().get("C") : null,
                request.getFormat() == QuestionFormat.MCQ ? request.getOptions().get("D") : null,
                request.getCorrectAnswer(),
                request.getExplanation()
        );

        questionRepository.save(question);
    }

    // ── Bulk upload ──────────────────────────────────────────────────────────

    @Override
    public UploadResponse uploadQuestions(MultipartFile file, String subjectId) {

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new BadRequestException("File name is missing");
        }

        List<String[]> rows;

        if (filename.toLowerCase().endsWith(".csv")) {
            rows = parseCsv(file);
        } else if (filename.toLowerCase().endsWith(".xlsx")) {
            rows = parseExcel(file);
        } else {
            throw new BadRequestException("Unsupported file type. Please upload a .csv or .xlsx file.");
        }

        if (!subjectRepository.existsByIdAndDeletedFalse(subjectId)) {
            throw new ResourceNotFoundException("Subject", subjectId);
        }

        return processRows(rows, subjectId);
    }

    // ── CSV parsing ──────────────────────────────────────────────────────────

    private List<String[]> parseCsv(MultipartFile file) {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String[]> all = reader.readAll();
            if (all.size() <= 1) {
                throw new BadRequestException("CSV file is empty or contains only headers");
            }
            return all.subList(1, all.size()); // skip header row

        } catch (IOException | CsvException e) {
            throw new BadRequestException("Failed to parse CSV: " + e.getMessage());
        }
    }

    // ── Excel parsing ────────────────────────────────────────────────────────

    private List<String[]> parseExcel(MultipartFile file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String[]> rows = new ArrayList<>();

            // Start at row index 1 (skip header at index 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 9 columns: category, format, questionText, A, B, C, D, correctAnswer, explanation
                String[] data = new String[9];
                for (int col = 0; col < 9; col++) {
                    data[col] = getCellStringValue(row.getCell(col));
                }
                rows.add(data);
            }

            if (rows.isEmpty()) {
                throw new BadRequestException("Excel file is empty or contains only headers");
            }

            return rows;

        } catch (IOException e) {
            throw new BadRequestException("Failed to parse Excel: " + e.getMessage());
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return cell.getStringCellValue().trim();
    }

    // ── Row processing ───────────────────────────────────────────────────────

    private UploadResponse processRows(List<String[]> rows, String subjectId) {
        int success = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2; // human-readable: data starts at row 2
            List<String> rowErrors = new ArrayList<>();

            String[] data = rows.get(i);
            if (data.length < 8) {
                errors.add("Row " + rowNum + ": insufficient columns (expected at least 8)");
                continue;
            }

            String categoryStr  = clean(data, 0);
            String formatStr    = clean(data, 1);
            String questionText = clean(data, 2);
            String optA         = clean(data, 3);
            String optB         = clean(data, 4);
            String optC         = clean(data, 5);
            String optD         = clean(data, 6);
            String correctAnswer = clean(data, 7);
            String explanation  = data.length > 8 ? clean(data, 8) : "";

            // Mandatory field presence
            if (categoryStr.isEmpty())  rowErrors.add("category is missing");
            if (formatStr.isEmpty())    rowErrors.add("format is missing");
            if (questionText.isEmpty()) rowErrors.add("questionText is missing");
            if (optA.isEmpty())         rowErrors.add("option A is missing");
            if (optB.isEmpty())         rowErrors.add("option B is missing");

            if (!rowErrors.isEmpty()) {
                errors.add("Row " + rowNum + ": " + String.join("; ", rowErrors));
                continue;
            }

            // Category validation
            QuestionCategory category;
            try {
                category = QuestionCategory.valueOf(categoryStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Row " + rowNum + ": invalid category '" + categoryStr + "' (use IMPORTANT or EXAM)");
                continue;
            }

            // Format validation
            QuestionFormat format;
            try {
                format = QuestionFormat.valueOf(formatStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Row " + rowNum + ": invalid format '" + formatStr + "' (use MCQ or TRUE_FALSE)");
                continue;
            }

            // Format-specific option validation
            if (format == QuestionFormat.MCQ) {
                if (optC.isEmpty()) rowErrors.add("option C is required for MCQ");
                if (optD.isEmpty()) rowErrors.add("option D is required for MCQ");
            }

            // Correct answer validation
            if (format == QuestionFormat.MCQ && !correctAnswer.matches("[ABCD]")) {
                rowErrors.add("correctAnswer must be A, B, C, or D for MCQ");
            } else if (format == QuestionFormat.TRUE_FALSE && !correctAnswer.matches("[AB]")) {
                rowErrors.add("correctAnswer must be A or B for TRUE_FALSE");
            }

            if (!rowErrors.isEmpty()) {
                errors.add("Row " + rowNum + ": " + String.join("; ", rowErrors));
                continue;
            }

            // Save
            questionRepository.save(buildQuestion(
                    subjectId, category, format, questionText,
                    optA, optB,
                    format == QuestionFormat.MCQ ? optC : null,
                    format == QuestionFormat.MCQ ? optD : null,
                    correctAnswer, explanation));
            success++;
        }

        return UploadResponse.builder()
                .total(rows.size())
                .success(success)
                .failed(rows.size() - success)
                .errors(errors)
                .build();
    }

    // ── Admin list ───────────────────────────────────────────────────────────

    @Override
    public Page<QuestionResponse> getQuestions(String subjectId, QuestionCategory category,
                                               QuestionFormat format, int page, int size) {

        List<Criteria> filters = new ArrayList<>();
        filters.add(Criteria.where("deleted").ne(true));
        if (subjectId != null) filters.add(Criteria.where("subjectId").is(subjectId));
        if (category  != null) filters.add(Criteria.where("category").is(category));
        if (format    != null) filters.add(Criteria.where("format").is(format));

        Query query = new Query();
        if (!filters.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filters.toArray(new Criteria[0])));
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        long total   = mongoTemplate.count(query, Question.class);
        List<Question> content = mongoTemplate.find(query.with(pageable), Question.class);

        return new PageImpl<>(content.stream().map(this::toResponse).toList(), pageable, total);
    }

    @Override
    public void deleteQuestion(String id) {
        Question question = questionRepository.findById(id)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));

        question.setDeleted(true);
        questionRepository.save(question);
    }

    @Override
    public QuestionResponse updateQuestion(String id, CreateQuestionRequest request) {
        Question question = questionRepository.findById(id)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));

        if (!subjectRepository.existsByIdAndDeletedFalse(request.getSubjectId())) {
            throw new ResourceNotFoundException("Subject", request.getSubjectId());
        }

        // Reuse existing format-aware validators (throw BadRequestException on failure)
        List<String> errors = new ArrayList<>();
        validateOptions(request.getOptions(), request.getFormat(), -1, errors);
        validateCorrectAnswer(request.getCorrectAnswer(), request.getFormat(), -1, errors);

        question.setSubjectId(request.getSubjectId());
        question.setCategory(request.getCategory());
        question.setFormat(request.getFormat());
        question.setQuestionText(request.getQuestionText().trim());
        question.setOptions(new Question.Options(
                request.getOptions().get("A"),
                request.getOptions().get("B"),
                request.getFormat() == QuestionFormat.MCQ ? request.getOptions().get("C") : null,
                request.getFormat() == QuestionFormat.MCQ ? request.getOptions().get("D") : null
        ));
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(request.getExplanation());

        return toResponse(questionRepository.save(question));
    }

    private QuestionResponse toResponse(Question q) {
        Map<String, String> opts = new LinkedHashMap<>();
        if (q.getOptions() != null) {
            Question.Options o = q.getOptions();
            if (o.getA() != null) opts.put("A", o.getA());
            if (o.getB() != null) opts.put("B", o.getB());
            if (o.getC() != null) opts.put("C", o.getC());
            if (o.getD() != null) opts.put("D", o.getD());
        }
        return QuestionResponse.builder()
                .id(q.getId())
                .subjectId(q.getSubjectId())
                .category(q.getCategory())
                .format(q.getFormat())
                .questionText(q.getQuestionText())
                .options(opts)
                .correctAnswer(q.getCorrectAnswer())
                .explanation(q.getExplanation())
                .createdAt(q.getCreatedAt())
                .build();
    }

    // ── Shared helpers ───────────────────────────────────────────────────────

    private Question buildQuestion(String subjectId, QuestionCategory category, QuestionFormat format,
                                   String questionText, String optA, String optB, String optC, String optD,
                                   String correctAnswer, String explanation) {
        return Question.builder()
                .subjectId(subjectId)
                .category(category)
                .format(format)
                .questionText(questionText)
                .options(new Question.Options(optA, optB, optC, optD))
                .correctAnswer(correctAnswer)
                .explanation(explanation)
                .build();
    }

    /**
     * Validates that required options are present and non-empty for the given format.
     * For MCQ: A, B, C, D required. For TRUE_FALSE: A, B required.
     * If rowNum <= 0, throws BadRequestException (used for manual create API).
     * Otherwise appends to errors list (used for bulk upload).
     */
    private void validateOptions(Map<String, String> options, QuestionFormat format,
                                 int rowNum, List<String> errors) {
        List<String> required = format == QuestionFormat.TRUE_FALSE
                ? List.of("A", "B")
                : List.of("A", "B", "C", "D");

        for (String key : required) {
            if (!options.containsKey(key) || options.get(key).isBlank()) {
                String msg = "Option " + key + " is missing or empty";
                if (rowNum <= 0) throw new BadRequestException(msg);
                else errors.add("Row " + rowNum + ": " + msg);
            }
        }
    }

    /**
     * Validates correctAnswer matches the allowed set for the format.
     * If rowNum <= 0, throws BadRequestException.
     */
    private void validateCorrectAnswer(String correctAnswer, QuestionFormat format,
                                       int rowNum, List<String> errors) {
        boolean valid = format == QuestionFormat.TRUE_FALSE
                ? correctAnswer.matches("[AB]")
                : correctAnswer.matches("[ABCD]");

        if (!valid) {
            String allowed = format == QuestionFormat.TRUE_FALSE ? "A or B" : "A, B, C, or D";
            String msg = "correctAnswer must be " + allowed + " for " + format;
            if (rowNum <= 0) throw new BadRequestException(msg);
            else errors.add("Row " + rowNum + ": " + msg);
        }
    }

    private String clean(String[] data, int index) {
        return (index < data.length && data[index] != null) ? data[index].trim() : "";
    }
}
