package com.example.payment.controller;

import com.example.payment.api.DefaultApi;
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
        exchange.getRequest().getHeaders().forEach((name, values) ->
                values.forEach(value -> log.debug("Заголовок запроса: {}={}", name, value))
        );

        return paymentService.getBalance(userId)
                .map(account -> {
                    Balance balance = new Balance();
                    balance.setAmount(account.getAmount());
                    return ResponseEntity.ok(balance);
                })
                .onErrorResume(e -> {
                    Balance balance = new Balance();
                    balance.setAmount(0.0);
                    return Mono.just(ResponseEntity.internalServerError().body(balance));
                });
    }


    @Override
    public Mono<ResponseEntity<Boolean>> processPayment(Double amount, Long userId, ServerWebExchange exchange) {
        exchange.getRequest().getHeaders().forEach((name, values) ->
                values.forEach(value -> log.debug("Заголовок запроса: {}={}", name, value))
        );

        return paymentService.processPayment(userId, amount)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Ошибка при обработке платежа для пользователя {}: {}", userId, e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(false));
                });
    }
}