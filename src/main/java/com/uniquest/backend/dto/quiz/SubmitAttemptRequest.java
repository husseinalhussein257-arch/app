package com.uniquest.backend.dto.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SubmitAttemptRequest {

    @NotEmpty(message = "answers list must not be empty")
    @Valid  // triggers validation on each AnswerRequest element
    private List<AnswerRequest> answers;
}
