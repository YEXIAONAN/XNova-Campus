package com.xnova.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTeacherDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String title;

    @NotBlank(message = "院系不能为空")
    private String department;

    private String major;

    private String contactOffice;
}
