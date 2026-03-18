package com.xnova.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long creatorId;

    private String questionType;

    private String stem;

    private String optionsJson;

    private String answerJson;

    private String analysis;

    private BigDecimal defaultScore;

    private Integer status;

    private String contentHash;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
