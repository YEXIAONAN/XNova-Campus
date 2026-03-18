package com.xnova.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("exam_answer_detail")
public class ExamAnswerDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long submissionId;

    private Long questionId;

    private String questionType;

    private String studentAnswerJson;

    private String standardAnswerJson;

    private BigDecimal autoScore;

    private BigDecimal manualScore;

    private BigDecimal finalScore;

    private Integer isCorrect;

    private String judgeStatus;
}

