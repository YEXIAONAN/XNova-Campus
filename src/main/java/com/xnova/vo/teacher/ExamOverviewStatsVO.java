package com.xnova.vo.teacher;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExamOverviewStatsVO {
    private Long publishId;
    private String className;
    private Integer submittedCount;
    private Integer reviewFinishedCount;
    private Integer pendingReviewCount;
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal passRate;
    private Long reviewTimes;
}
