package com.example.main_service.controller;

import com.example.main_service.client.payment.model.Balance;
import com.example.main_service.service.PaymentClientService;
import com.example.main_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentClientService paymentClientService;
    private final UserService userService;

    public PaymentController(PaymentClientService paymentClientService, UserService userService) {
        this.paymentClientService = paymentClientService;
        this.userService = userService;
    }

    @GetMapping("/balance")
    public Mono<ResponseEntity<Balance>> getBalance() {
        log.info("=== Начало процесса получения баланса ===");
        
        // Логируем информацию об аутентификации
        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(this::logSecurityContext)
                .then(userService.getUserIdFromSecurityContext()
                    .doOnNext(userId -> log.info("Получен userId: {} из контекста безопасности", userId))
                    .flatMap(userId -> {
                        log.info("Отправляем запрос на получение баланса для пользователя: {}", userId);
                        return paymentClientService.getBalance(userId)
                                .doOnNext(balance -> log.info("Получен ответ от payment-сервиса с балансом: {}", balance.getAmount()))
                                .map(ResponseEntity::ok)
                                .doOnNext(response -> log.info("Сформирован ответ с кодом: {}", response.getStatusCode()))
                                .onErrorResume(e -> {
                                    log.error("Ошибка при получении баланса: {}", e.getMessage(), e);
                                    Balance balance = new Balance();
                                    balance.setAmount(0.0);
                                    return Mono.just(ResponseEntity.internalServerError().body(balance));
                                });
                    })
                    .doOnSuccess(response -> log.info("=== Успешное завершение запроса баланса ==="))
                    .doOnError(e -> log.error("=== Ошибка при запросе баланса: {} ===", e.getMessage(), e))
                );
    }

    @PostMapping("/process")
    public Mono<ResponseEntity<Boolean>> processPayment(@RequestParam Double amount) {
        log.info("=== Начало процесса оплаты на сумму: {} ===", amount);
        
        // Логируем информацию об аутентификации
        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(this::logSecurityContext)
                .then(userService.getUserIdFromSecurityContext()
                    .doOnNext(userId -> log.info("Получен userId: {} из контекста безопасности", userId))
                    .flatMap(userId -> {
                        log.info("Отправляем запрос на оплату суммы {} для пользователя: {}", amount, userId);
                        return paymentClientService.processPayment(userId, amount)
                                .doOnNext(result -> log.info("Получен ответ от payment-сервиса: {}", result ? "Платеж успешен" : "Платеж отклонен"))
                                .map(ResponseEntity::ok)
                                .doOnNext(response -> log.info("Сформирован ответ с кодом: {}", response.getStatusCode()))
                                .onErrorResume(e -> {
                                    log.error("Ошибка при обработке платежа: {}", e.getMessage(), e);
                                    return Mono.just(ResponseEntity.badRequest().body(false));
                                });
                    })
                    .doOnSuccess(response -> log.info("=== Успешное завершение процесса оплаты ==="))
                    .doOnError(e -> log.error("=== Ошибка при обработке платежа: {} ===", e.getMessage(), e))
                );
    }
    
    /**
     * Логирует информацию о контексте безопасности
     */
    private void logSecurityContext(SecurityContext context) {
        Authentication auth = context.getAuthentication();
        if (auth != null) {
            log.info("Пользователь аутентифицирован: {}, роли: {}", 
                    auth.getName(), 
                    auth.getAuthorities());
            
            // Логируем детали аутентификации
            Object principal = auth.getPrincipal();
            log.debug("Тип principal: {}", principal.getClass().getName());
            
            Object credentials = auth.getCredentials();
            if (credentials != null) {
                log.debug("Тип credentials: {}", credentials.getClass().getName());
            } else {
                log.debug("Credentials: null");
            }
        } else {
            log.warn("Аутентификация отсутствует в контексте безопасности");
        }
    }
} 