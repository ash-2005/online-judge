package com.oj.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthSupport {

    private AuthSupport() {
    }

    public static UserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("Not authenticated");
        }
        return principal;
    }

    public static String currentUsername() {
        return currentUser().username();
    }

    public static Long currentUserId() {
        return currentUser().userId();
    }
}
