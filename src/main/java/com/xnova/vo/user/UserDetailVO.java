package com.xnova.vo.user;

import lombok.Data;

@Data
public class UserDetailVO {
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String userType;
    private Integer status;
    private String roleCode;

    private String studentNo;
    private Integer enrollmentYear;
    private String grade;
    private String className;
    private String contactAddress;

    private String teacherNo;
    private String title;
    private String department;
    private String contactOffice;

    private String major;
}

