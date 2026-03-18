package com.xnova.vo.student;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentExamResultVO {
    private Long publishId;
    private Long submissionId;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private BigDecimal totalScore;
    private String submissionStatus;
    private String scoreStatus;
}

