package com.xnova.vo.teacher;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PendingReviewVO {
    private Long submissionId;
    private Long publishId;
    private String examName;
    private Long studentId;
    private String studentName;
    private String className;
    private String grade;
    private LocalDateTime submitTime;
    private BigDecimal objectiveScore;
    private String reviewStatus;
    private String scoreStatus;
}
