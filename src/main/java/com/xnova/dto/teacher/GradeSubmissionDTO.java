package com.xnova.dto.teacher;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GradeSubmissionDTO {

    @Valid
    @NotEmpty(message = "批改列表不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "答题明细ID不能为空")
        private Long answerDetailId;

        @NotNull(message = "主观题分数不能为空")
        @DecimalMin(value = "0", message = "分数不能小于0")
        private BigDecimal manualScore;

        private String comment;
    }
}
