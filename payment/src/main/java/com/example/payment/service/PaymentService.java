package com.example.payment.service;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PaymentService {
    Mono<BigDecimal> getBalance();
    Mono<Boolean> processPayment(BigDecimal amount);
}
