package com.xnova.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_publish")
public class ExamPublish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private String examName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationMinutes;

    private String targetScopeType;

    private String targetScopeValue;

    private Integer status;
}

