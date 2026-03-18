package com.xnova.service.impl.auth;

import com.xnova.dto.auth.LoginDTO;
import com.xnova.entity.SysUser;
import com.xnova.exception.BizException;
import com.xnova.security.LoginPrincipal;
import com.xnova.service.auth.AuthService;
import com.xnova.utils.JwtUtil;
import com.xnova.vo.auth.CurrentUserVO;
import com.xnova.vo.auth.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = mockUser(dto.getUsername());
        if (user == null || user.getDeleted() == 1 || user.getStatus() == 0) {
            throw new BizException(401, "账号不存在或已禁用");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }

        List<String> roles = resolveRoles(user.getUsername());
        List<String> permissions = resolvePermissions(roles);

        if (dto.getLoginType() != null && !dto.getLoginType().isBlank() && !roles.contains(dto.getLoginType())) {
            throw new BizException(403, "登录身份与账号角色不匹配");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("perms", permissions);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), claims);

        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setExpiresIn(7200L);
        vo.setRoles(roles);
        vo.setPermissions(permissions);

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        vo.setUser(userInfo);
        return vo;
    }

    @Override
    public CurrentUserVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginPrincipal principal)) {
            throw new BizException(401, "未认证或Token已失效");
        }

        CurrentUserVO vo = new CurrentUserVO();
        vo.setId(principal.getUserId());
        vo.setUsername(principal.getUsername());
        vo.setRealName(mockRealName(principal.getUsername()));
        vo.setRoles(principal.getRoles());
        vo.setPermissions(principal.getPermissions());
        return vo;
    }

    @Override
    public void logout(String authorization) {
        // MVP无状态JWT: 前端删除token即可。
        // 生产建议: 解析jti并写入Redis黑名单。
    }

    private SysUser mockUser(String username) {
        if (!List.of("admin", "teacher1", "student1").contains(username)) {
            return null;
        }
        SysUser user = new SysUser();
        user.setId(switch (username) {
            case "admin" -> 1L;
            case "teacher1" -> 2L;
            default -> 3L;
        });
        user.setUsername(username);
        user.setRealName(mockRealName(username));
        user.setStatus(1);
        user.setDeleted(0);
        // 123456
        user.setPassword("$2a$10$3euPcmQFCiblsZeEu5s7pOyA4j7M7xqY21D2ztiv0V9.N20fz4S4.");
        return user;
    }

    private String mockRealName(String username) {
        return switch (username) {
            case "admin" -> "系统管理员";
            case "teacher1" -> "王老师";
            default -> "张三";
        };
    }

    private List<String> resolveRoles(String username) {
        return switch (username) {
            case "admin" -> List.of("ADMIN");
            case "teacher1" -> List.of("TEACHER");
            default -> List.of("STUDENT");
        };
    }

    private List<String> resolvePermissions(List<String> roles) {
        if (roles.contains("ADMIN")) {
            return List.of("user:add", "user:edit", "exam:publish", "score:view:all");
        }
        if (roles.contains("TEACHER")) {
            return List.of("exam:publish", "paper:grade", "score:view:class");
        }
        return List.of("exam:submit", "score:view:self", "rank:view");
    }
}

