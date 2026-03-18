package com.xnova.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("paper")
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long creatorId;

    private String paperName;

    private String description;

    private BigDecimal totalScore;

    private Integer durationMinutes;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
