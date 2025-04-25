package com.example.payment.controller;

import com.example.payment.api.DefaultApi;
import com.example.payment.model.dto.Balance;
import com.example.payment.model.dto.Error;
import com.example.payment.service.PaymentService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.ResponseEntity;

@RestController
public class PaymentController implements DefaultApi {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Mono<ResponseEntity<Balance>> getBalance(ServerWebExchange exchange) {
        return paymentService.getBalance()
                .map(account -> {
                    Balance balance = new Balance();
                    balance.setAmount(account.getAmount());
                    return ResponseEntity.ok(balance);
                })
                .onErrorResume(e -> {
                    // Создаем пустой баланс для ошибочного случая
                    Balance balance = new Balance();
                    balance.setAmount(0.0);
                    return Mono.just(ResponseEntity.internalServerError().body(balance));
                });
    }

    @Override
    public Mono<ResponseEntity<Boolean>> processPayment(Double amount, ServerWebExchange exchange) {
        return paymentService.processPayment(amount)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(false)));
    }
} 