package com.example.main_service.service;

import com.example.main_service.model.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> findByUsername(String username);
    Mono<User> createUser(String username, String password, String role);
} 