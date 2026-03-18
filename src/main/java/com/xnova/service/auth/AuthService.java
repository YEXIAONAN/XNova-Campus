package com.xnova.service.auth;

import com.xnova.dto.auth.LoginDTO;
import com.xnova.vo.auth.CurrentUserVO;
import com.xnova.vo.auth.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);

    CurrentUserVO getCurrentUser();

    void logout(String authorization);
}

