package com.example.main_service.service;

import com.example.main_service.model.User;
import com.example.main_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<Long> findUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId);
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Mono<User> createUser(String username, String password, String role) {
        return userRepository.findByUsername(username)
            .flatMap(existingUser -> Mono.<User>error(new RuntimeException("Username already exists")))
            .switchIfEmpty(
                Mono.just(new User())
                    .map(user -> {
                        user.setUsername(username);
                        user.setPassword(passwordEncoder.encode(password));
                        user.setRole(role);
                        return user;
                    })
                    .flatMap(userRepository::save)
            );
    }

    @Override
    public Mono<Long> getUserIdFromSecurityContext() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    // Получаем principal из аутентификации
                    Object principal = authentication.getPrincipal();
                    String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                    return this.findUserIdByUsername(username)
                            .doOnNext(userId -> log.debug("Найден userId по username: {}", userId));
                })
                .doOnError(e -> log.error("Ошибка при получении userId", e));
    }
} 