package com.xnova.vo.teacher;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDetailVO {
    private Long submissionId;
    private Long publishId;
    private String examName;
    private Long studentId;
    private String studentName;
    private String className;
    private String grade;
    private LocalDateTime submitTime;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private BigDecimal totalScore;
    private String reviewStatus;
    private String scoreStatus;
    private List<QuestionReviewItemVO> questions;

    @Data
    public static class QuestionReviewItemVO {
        private Long answerDetailId;
        private Long questionId;
        private String questionType;
        private String stem;
        private String standardAnswer;
        private String studentAnswer;
        private BigDecimal maxScore;
        private BigDecimal autoScore;
        private BigDecimal manualScore;
        private BigDecimal finalScore;
        private String judgeStatus;
    }
}
