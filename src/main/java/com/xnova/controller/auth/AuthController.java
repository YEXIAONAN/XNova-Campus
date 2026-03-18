package com.xnova.controller.auth;

import com.xnova.common.result.ApiResponse;
import com.xnova.dto.auth.LoginDTO;
import com.xnova.service.auth.AuthService;
import com.xnova.vo.auth.CurrentUserVO;
import com.xnova.vo.auth.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return ApiResponse.ok(authService.login(dto));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVO> me() {
        return ApiResponse.ok(authService.getCurrentUser());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return ApiResponse.ok();
    }
}

