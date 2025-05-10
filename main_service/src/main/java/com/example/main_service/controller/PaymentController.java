package com.example.main_service.controller;

import com.example.main_service.client.payment.model.Balance;
import com.example.main_service.service.PaymentClientService;
import com.example.main_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentClientService paymentClientService;
    private final UserService userService;

    public PaymentController(PaymentClientService paymentClientService, UserService userService) {
        this.paymentClientService = paymentClientService;
        this.userService = userService;
    }

    @GetMapping("/balance")
    public Mono<ResponseEntity<Balance>> getBalance() {
        return ReactiveSecurityContextHolder.getContext()
                .then(userService.getUserIdFromSecurityContext()
                        .flatMap(userId -> {
                            return paymentClientService.getBalance(userId)
                                    .map(ResponseEntity::ok)
                                    .onErrorResume(e -> {
                                        log.error("Ошибка при получении баланса: {}", e.getMessage(), e);
                                        Balance balance = new Balance();
                                        balance.setAmount(0.0);
                                        return Mono.just(ResponseEntity.internalServerError().body(balance));
                                    });
                        })
                        .doOnError(e -> log.error("Ошибка при запросе баланса: {}", e.getMessage(), e))
                );
    }

    @PostMapping("/process")
    public Mono<ResponseEntity<Boolean>> processPayment(@RequestParam Double amount) {
        return ReactiveSecurityContextHolder.getContext()
                .then(userService.getUserIdFromSecurityContext()
                        .flatMap(userId -> paymentClientService.processPayment(userId, amount)
                                .map(ResponseEntity::ok)
                                .onErrorResume(e -> {
                                    log.error("Ошибка при обработке платежа: {}", e.getMessage(), e);
                                    return Mono.just(ResponseEntity.badRequest().body(false));
                                }))
                        .doOnError(e -> log.error("Ошибка при обработке платежа: {}", e.getMessage(), e))
                );
    }
} 