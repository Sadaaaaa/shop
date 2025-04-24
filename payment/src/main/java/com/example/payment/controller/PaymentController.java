package com.example.payment.controller;

import com.example.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/balance")
    public Mono<ResponseEntity<BigDecimal>> getBalance() {
        return paymentService.getBalance()
                .map(ResponseEntity::ok);
    }

    @PostMapping("/process")
    public Mono<ResponseEntity<Boolean>> processPayment(@RequestParam BigDecimal amount) {
        return paymentService.processPayment(amount)
                .map(success -> {
                    if (success) {
                        return ResponseEntity.ok(true);
                    } else {
                        return ResponseEntity.badRequest().body(false);
                    }
                });
    }
} 