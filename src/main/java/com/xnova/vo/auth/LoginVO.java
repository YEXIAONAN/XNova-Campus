package com.xnova.vo.auth;

import lombok.Data;

import java.util.List;

@Data
public class LoginVO {
    private String tokenType = "Bearer";
    private String accessToken;
    private Long expiresIn;
    private UserInfo user;
    private List<String> roles;
    private List<String> permissions;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
    }
}

