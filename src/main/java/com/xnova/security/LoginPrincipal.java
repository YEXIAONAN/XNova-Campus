package com.xnova.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginPrincipal {
    private Long userId;
    private String username;
    private List<String> roles;
    private List<String> permissions;
}
