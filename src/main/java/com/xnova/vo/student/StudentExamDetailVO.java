package com.xnova.vo.student;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudentExamDetailVO {

    private Long publishId;
    private Long paperId;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Long remainSeconds;
    private BigDecimal totalScore;
    private List<QuestionItemVO> questions;

    @Data
    public static class QuestionItemVO {
        private Long questionId;
        private String questionType;
        private String stem;
        private List<String> options;
        private Integer questionOrder;
        private BigDecimal score;
    }
}

