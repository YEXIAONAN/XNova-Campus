package com.xnova.utils;

import com.xnova.exception.BizException;
import com.xnova.security.LoginPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginPrincipal principal)) {
            throw new BizException(401, "未认证或Token已失效");
        }
        return principal.getUserId();
    }
}

