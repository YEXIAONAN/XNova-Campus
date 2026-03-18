package com.xnova.dto.teacher;

import com.xnova.common.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateQuestionDTO {

    @NotNull(message = "题型不能为空")
    private QuestionType questionType;

    @NotBlank(message = "题干不能为空")
    private String stem;

    private List<String> options;

    private String answer;

    private List<String> answers;

    private String analysis;

    private BigDecimal defaultScore;
}

