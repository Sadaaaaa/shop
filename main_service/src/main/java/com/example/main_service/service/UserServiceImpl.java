package com.example.main_service.service;

import com.example.main_service.model.User;
import com.example.main_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
} 