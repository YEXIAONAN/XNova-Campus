package com.xnova.vo.auth;

import lombok.Data;

import java.util.List;

@Data
public class CurrentUserVO {
    private Long id;
    private String username;
    private String realName;
    private List<String> roles;
    private List<String> permissions;
}

