package com.xnova.dto.teacher;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AddPaperQuestionsDTO {

    @NotEmpty(message = "题目列表不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "题目ID不能为空")
        private Long questionId;

        private Integer questionOrder;

        private BigDecimal score;

        private Integer required;
    }
}

