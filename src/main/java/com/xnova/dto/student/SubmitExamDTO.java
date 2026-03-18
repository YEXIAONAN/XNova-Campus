package com.xnova.dto.student;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitExamDTO {

    @NotNull(message = "考试时长不能为空")
    @Min(value = 1, message = "考试时长必须大于0")
    private Integer durationSeconds;

    @Valid
    @NotEmpty(message = "答案不能为空")
    private List<AnswerItemDTO> answers;

    @Data
    public static class AnswerItemDTO {
        @NotNull(message = "题目ID不能为空")
        private Long questionId;

        private String answer;

        private List<String> answers;
    }
}

