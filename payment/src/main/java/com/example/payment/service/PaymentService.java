package com.example.payment.service;

import com.example.payment.model.PaymentAccount;
import com.example.payment.repository.PaymentAccountRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PaymentService {
    private final PaymentAccountRepository paymentAccountRepository;

    public PaymentService(PaymentAccountRepository paymentAccountRepository) {
        this.paymentAccountRepository = paymentAccountRepository;
    }

    public Mono<PaymentAccount> getBalance() {
        return paymentAccountRepository.findById(1L)
                .switchIfEmpty(Mono.error(new RuntimeException("Payment account not found")));
    }

    public Mono<Boolean> processPayment(Double amount) {
        return paymentAccountRepository.findById(1L)
                .switchIfEmpty(Mono.error(new RuntimeException("Payment account not found")))
                .flatMap(account -> {
                    if (account.getAmount() < amount) {
                        return Mono.error(new RuntimeException("Insufficient funds"));
                    }
                    account.setAmount(account.getAmount() - amount);
                    return paymentAccountRepository.save(account)
                            .thenReturn(true);
                });
    }
} 