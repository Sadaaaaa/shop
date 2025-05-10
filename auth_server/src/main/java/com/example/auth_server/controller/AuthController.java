package com.example.auth_server.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/user")
    public Map<String, Object> user() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("authorities", authentication.getAuthorities());
        return userInfo;
    }

    @GetMapping("/health")
    public String health() {
        return "Auth server is running";
    }
} 