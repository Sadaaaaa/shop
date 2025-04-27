package com.example.payment.service;

import com.example.payment.model.PaymentAccount;
import com.example.payment.repository.PaymentAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PaymentService {
    private final PaymentAccountRepository paymentAccountRepository;

    public PaymentService(PaymentAccountRepository paymentAccountRepository) {
        this.paymentAccountRepository = paymentAccountRepository;
    }

    public Mono<PaymentAccount> getBalance(long userId) {
        return paymentAccountRepository.findByUserId(userId)
                .doOnSuccess(account -> {
                    if (account != null) {
                        log.info("Найден баланс для пользователя {}: {}", userId, account.getAmount());
                    } else {
                        log.warn("Аккаунт не найден для пользователя: {}", userId);
                    }
                })
                .doOnError(error -> log.error("Ошибка при получении баланса для пользователя {}: {}", userId, error.getMessage()))
                .switchIfEmpty(Mono.error(new RuntimeException("Payment account not found")));
    }

    public Mono<Boolean> processPayment(long userId, Double amount) {
        return paymentAccountRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Payment account not found")))
                .flatMap(account -> {
                    if (account.getAmount() < amount) {
                        return Mono.error(new RuntimeException("Insufficient funds"));
                    }
                    account.setAmount(account.getAmount() - amount);
                    return paymentAccountRepository.save(account)
                            .thenReturn(true);
                })
                .doOnSuccess(result -> log.info("Платеж успешно обработан для пользователя: {}", userId))
                .doOnError(error -> log.error("Ошибка при обработке платежа для пользователя {}: {}", userId, error.getMessage()));
    }
} 