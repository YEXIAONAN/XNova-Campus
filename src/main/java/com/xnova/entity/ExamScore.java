package com.xnova.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_score")
public class ExamScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long publishId;

    private Long studentId;

    private Long submissionId;

    private String className;

    private String grade;

    private BigDecimal totalScore;

    private Integer rankClass;

    private Integer rankGrade;

    private String scoreStatus;

    private LocalDateTime createdAt;
}

