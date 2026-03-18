package com.xnova.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStudentDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String password;

    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotNull(message = "入学年份不能为空")
    private Integer enrollmentYear;

    @NotBlank(message = "专业不能为空")
    private String major;

    @NotBlank(message = "年级不能为空")
    private String grade;

    @NotBlank(message = "班级不能为空")
    private String className;

    private String contactAddress;
}

