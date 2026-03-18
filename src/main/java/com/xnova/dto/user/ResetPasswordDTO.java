package com.xnova.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String newPassword;
}
