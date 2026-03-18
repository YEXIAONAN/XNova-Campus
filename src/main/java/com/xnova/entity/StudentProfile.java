package com.xnova.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student_profile")
public class StudentProfile {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String studentNo;

    private Integer enrollmentYear;

    private String major;

    private String grade;

    private String className;

    private String contactAddress;
}

