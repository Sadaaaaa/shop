package com.example.main_service.controller;

import com.example.main_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(ServerWebExchange exchange, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        
        // Проверяем параметры запроса
        exchange.getRequest().getQueryParams().forEach((key, values) -> {
            if (!values.isEmpty()) {
                model.addAttribute(key, true);
            }
        });
        
        return "login";
    }

    @GetMapping("/register")
    public Mono<String> registerPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return Mono.just("redirect:/");
        }
        return Mono.just("register");
    }

    @PostMapping("/register")
    public Mono<String> registerUser(ServerWebExchange exchange) {
        exchange.getResponse().getHeaders().add("Content-Type", "text/html;charset=UTF-8");
        
        return exchange.getFormData()
            .flatMap(formData -> {
                String username = formData.getFirst("username");
                String password = formData.getFirst("password");
                String role = formData.getFirst("role");
                
                if (role == null || role.trim().isEmpty()) {
                    role = "USER";
                }

                log.info("Получены данные формы: username={}, role={}", 
                        username != null ? "provided" : "null",
                        role);

                if (username == null || username.trim().isEmpty() || 
                    password == null || password.trim().isEmpty()) {
                    log.warn("Попытка регистрации с пустыми данными: username={}, password={}", 
                            username != null ? "provided" : "null",
                            password != null ? "provided" : "null");
                    return Mono.just("redirect:/register?error=invalid");
                }

                return userService.createUser(username.trim(), password.trim(), role)
                    .doOnSuccess(user -> log.info("Успешная регистрация пользователя: {}", username))
                    .doOnError(e -> log.error("Ошибка при регистрации пользователя {}: {}", username, e.getMessage()))
                    .thenReturn("redirect:/login?registered")
                    .onErrorResume(e -> {
                        if (e.getMessage().contains("Username already exists")) {
                            return Mono.just("redirect:/register?error=exists");
                        }
                        return Mono.just("redirect:/register?error=unknown");
                    });
            });
    }
} 