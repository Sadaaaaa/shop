package com.example.main_service.service;

import com.example.main_service.model.User;
import reactor.core.publisher.Mono;

/**
 * Сервис для работы с пользователями
 */
public interface UserService {
    
    /**
     * Найти ID пользователя по его имени пользователя (логину)
     * @param username имя пользователя
     * @return Mono с ID пользователя или пустой Mono, если пользователь не найден
     */
    Mono<Long> findUserIdByUsername(String username);

    Mono<User> findByUsername(String username);
    Mono<User> createUser(String username, String password, String role);
    Mono<Long> getUserIdFromSecurityContext();
} 