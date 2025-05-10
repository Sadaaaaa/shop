package com.example.main_service.controller;

import com.example.main_service.client.payment.model.Balance;
import com.example.main_service.service.PaymentClientService;
import com.example.main_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentClientService paymentClientService;
    private final UserService userService;

    public PaymentController(PaymentClientService paymentClientService, UserService userService) {
        this.paymentClientService = paymentClientService;
        this.userService = userService;
    }

    @GetMapping("/balance")
    public Mono<ResponseEntity<Balance>> getBalance() {
        log.debug("Получен запрос на получение баланса");
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> {
                    log.debug("Получен userId: {}", userId);
                    return paymentClientService.getBalance(userId)
                            .map(ResponseEntity::ok)
                            .onErrorResume(e -> {
                                log.error("Ошибка при получении баланса", e);
                                Balance balance = new Balance();
                                balance.setAmount(0.0);
                                return Mono.just(ResponseEntity.internalServerError().body(balance));
                            });
                })
                .doOnError(e -> log.error("Ошибка в методе getBalance", e));
    }

    @PostMapping("/process")
    public Mono<ResponseEntity<Boolean>> processPayment(
            @RequestParam Double amount) {
        log.debug("Получен запрос на обработку платежа на сумму: {}", amount);
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> {
                    log.debug("Получен userId для платежа: {}", userId);
                    return paymentClientService.processPayment(userId, amount)
                            .map(ResponseEntity::ok)
                            .onErrorResume(e -> {
                                log.error("Ошибка при обработке платежа", e);
                                return Mono.just(ResponseEntity.badRequest().body(false));
                            });
                })
                .doOnError(e -> log.error("Ошибка в методе processPayment", e));
    }
} 