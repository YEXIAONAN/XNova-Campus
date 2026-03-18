package com.xnova.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_submission")
public class ExamSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long publishId;

    private Long studentId;

    private LocalDateTime submitTime;

    private BigDecimal objectiveScore;

    private BigDecimal subjectiveScore;

    private BigDecimal totalScore;

    private String status;

    private String reviewStatus;

    private String scoreStatus;

    private Integer durationSeconds;

    private Integer isTimeout;
}

