package com.xnova.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTeacherDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String password;

    @NotBlank(message = "工号不能为空")
    private String teacherNo;

    private String title;

    @NotBlank(message = "院系不能为空")
    private String department;

    private String major;

    private String contactOffice;
}
