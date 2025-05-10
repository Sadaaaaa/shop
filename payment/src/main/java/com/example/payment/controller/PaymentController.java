package com.example.payment.controller;

import com.example.payment.api.DefaultApi;
import com.example.payment.config.SecurityUtils;
import com.example.payment.model.dto.Balance;
import com.example.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@CrossOrigin("*")
@RestController
public class PaymentController implements DefaultApi {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Mono<ResponseEntity<Balance>> getBalance(Long userId, ServerWebExchange exchange) {
        log.info("=== Получен запрос на получение баланса ===");
        exchange.getRequest().getHeaders().forEach((name, values) -> 
            values.forEach(value -> log.debug("Заголовок запроса: {}={}", name, value))
        );
        
        // Если userId не предоставлен, получаем его из JWT токена
        if (userId == null) {
            log.info("userId не указан в запросе, пытаемся извлечь из JWT токена");
            return SecurityUtils.getUserId()
                    .doOnNext(id -> log.info("Извлечен userId из JWT токена: {}", id))
                    .flatMap(this::getBalanceByUserId)
                    .switchIfEmpty(Mono.defer(() -> {
                        log.warn("Не удалось извлечь userId из JWT токена");
                        return Mono.just(createErrorBalanceResponse("Не удалось определить пользователя"));
                    }));
        }
        
        log.info("Используем userId из запроса: {}", userId);
        return getBalanceByUserId(userId);
    }

    private Mono<ResponseEntity<Balance>> getBalanceByUserId(Long userId) {
        log.debug("Запрос баланса для пользователя: {}", userId);
        return paymentService.getBalance(userId)
                .doOnNext(account -> log.info("Получен баланс для пользователя {}: {}", userId, account.getAmount()))
                .map(account -> {
                    Balance balance = new Balance();
                    balance.setAmount(account.getAmount());
                    log.debug("Сформирован ответ с балансом: {}", balance.getAmount());
                    return ResponseEntity.ok(balance);
                })
                .onErrorResume(e -> {
                    log.error("Ошибка при получении баланса для пользователя {}: {}", userId, e.getMessage());
                    return Mono.just(createErrorBalanceResponse("Ошибка при получении баланса"));
                });
    }

    @Override
    public Mono<ResponseEntity<Boolean>> processPayment(Double amount, Long userId, ServerWebExchange exchange) {
        log.info("=== Получен запрос на обработку платежа на сумму: {} ===", amount);
        exchange.getRequest().getHeaders().forEach((name, values) -> 
            values.forEach(value -> log.debug("Заголовок запроса: {}={}", name, value))
        );
        
        // Если userId не предоставлен, получаем его из JWT токена
        if (userId == null) {
            log.info("userId не указан в запросе, пытаемся извлечь из JWT токена");
            return SecurityUtils.getUserId()
                    .doOnNext(id -> log.info("Извлечен userId из JWT токена: {}", id))
                    .flatMap(id -> processPaymentForUserId(id, amount))
                    .switchIfEmpty(Mono.defer(() -> {
                        log.warn("Не удалось извлечь userId из JWT токена");
                        return Mono.just(ResponseEntity.badRequest().body(false));
                    }));
        }
        
        log.info("Используем userId из запроса: {}", userId);
        return processPaymentForUserId(userId, amount);
    }

    private Mono<ResponseEntity<Boolean>> processPaymentForUserId(Long userId, Double amount) {
        log.debug("Обработка платежа на сумму {} для пользователя: {}", amount, userId);
        return paymentService.processPayment(userId, amount)
                .doOnNext(result -> log.info("Результат обработки платежа для пользователя {}: {}", userId, result ? "успешно" : "отклонено"))
                .map(result -> {
                    log.debug("Сформирован ответ с результатом: {}", result);
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(e -> {
                    log.error("Ошибка при обработке платежа для пользователя {}: {}", userId, e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(false));
                });
    }

    private ResponseEntity<Balance> createErrorBalanceResponse(String message) {
        log.error(message);
        Balance balance = new Balance();
        balance.setAmount(0.0);
        return ResponseEntity.internalServerError().body(balance);
    }
} 