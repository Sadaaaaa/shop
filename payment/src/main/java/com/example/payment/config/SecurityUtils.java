package com.example.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

/**
 * Утилитный класс для работы с JWT токенами и безопасностью
 */
@Slf4j
public class SecurityUtils {

    /**
     * Получает ID пользователя из JWT токена
     *
     * @return Mono с ID пользователя или пустой Mono, если пользователь не аутентифицирован
     */
    public static Mono<Long> getUserId() {
        log.debug("Начало извлечения userId из JWT токена");
        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(context -> log.debug("Получен SecurityContext"))
                .map(SecurityContext::getAuthentication)
                .doOnNext(auth -> {
                    if (auth != null) {
                        log.debug("Получена аутентификация: {}, principal типа: {}", 
                                auth.getName(), 
                                auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
                    } else {
                        log.warn("Аутентификация отсутствует в контексте безопасности");
                    }
                })
                .flatMap(SecurityUtils::extractUserId)
                .doOnNext(userId -> log.debug("Извлечен userId: {}", userId))
                .doOnError(e -> log.error("Ошибка при получении userId из токена", e));
    }

    /**
     * Извлекает ID пользователя из аутентификации
     *
     * @param authentication аутентификация
     * @return Mono с ID пользователя или пустой Mono
     */
    private static Mono<Long> extractUserId(Authentication authentication) {
        log.debug("Извлечение userId из аутентификации: {}", authentication.getName());
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            log.debug("JWT токен получен, subject: {}, claims: {}", jwt.getSubject(), jwt.getClaims().keySet());
            
            // Извлекаем subject из токена, который должен содержать ID пользователя
            String subject = jwt.getSubject();
            if (subject != null) {
                try {
                    Long userId = Long.parseLong(subject);
                    log.debug("Извлечен userId из subject: {}", userId);
                    return Mono.just(userId);
                } catch (NumberFormatException e) {
                    log.error("Не удалось преобразовать subject '{}' в Long", subject, e);
                }
            }
            
            // Если subject не содержит ID пользователя, пробуем извлечь из поля "user_id" или аналогичных
            Object userId = jwt.getClaims().get("user_id");
            if (userId == null) {
                // Пробуем поле userId (которое мы добавили в auth_server)
                userId = jwt.getClaims().get("userId");
                log.debug("Ищем userId в поле 'userId': {}", userId);
            }
            
            if (userId != null) {
                log.debug("Найдено поле userId в claims: {}, тип: {}", userId, userId.getClass().getName());
                try {
                    if (userId instanceof Number) {
                        Long id = ((Number) userId).longValue();
                        log.debug("Извлечен userId из поля userId (Number): {}", id);
                        return Mono.just(id);
                    } else if (userId instanceof String) {
                        Long id = Long.parseLong((String) userId);
                        log.debug("Извлечен userId из поля userId (String): {}", id);
                        return Mono.just(id);
                    }
                } catch (NumberFormatException e) {
                    log.error("Не удалось преобразовать userId '{}' в Long", userId, e);
                }
            } else {
                log.debug("Поля user_id и userId не найдены в JWT токене");
                
                // Выводим все доступные claims для отладки
                log.debug("Доступные claims в JWT: {}", jwt.getClaims());
            }
        } else {
            log.debug("Principal не является JWT токеном: {}", principal.getClass().getName());
        }
        
        log.warn("Не удалось извлечь userId из аутентификации");
        return Mono.empty();
    }
} 