package com.example.payment.controller;

import com.example.payment.api.DefaultApi;
import com.example.payment.model.dto.Balance;
import com.example.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@CrossOrigin("*")
@RestController
public class PaymentController implements DefaultApi {
    private final PaymentService paymentService;

    private static final Long MOCK_USER = 1L;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Mono<ResponseEntity<Balance>> getBalance(ServerWebExchange exchange) {
        return paymentService.getBalance(MOCK_USER)
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
    public Mono<ResponseEntity<Boolean>> processPayment(Double amount, ServerWebExchange exchange) {
        return paymentService.processPayment(MOCK_USER, amount)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(false)));
    }
} 