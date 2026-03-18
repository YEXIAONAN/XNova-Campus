package com.xnova.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_review_log")
public class TeacherReviewLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private Long submissionId;

    private Long publishId;

    private String actionType;

    private LocalDateTime createdAt;
}
