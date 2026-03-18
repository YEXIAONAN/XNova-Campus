package com.xnova.vo.teacher;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaperDetailVO {
    private Long id;
    private String paperName;
    private String description;
    private Integer durationMinutes;
    private BigDecimal totalScore;
    private List<PaperQuestionItemVO> questions;

    @Data
    public static class PaperQuestionItemVO {
        private Long questionId;
        private Integer questionOrder;
        private BigDecimal score;
        private String questionType;
        private String stem;
        private List<String> options;
    }
}

