package com.oj.api.security;

public record UserPrincipal(Long userId, String username, String role) {
}
