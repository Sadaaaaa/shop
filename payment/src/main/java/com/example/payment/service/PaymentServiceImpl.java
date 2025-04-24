package com.example.payment.service;

import com.example.payment.model.PaymentAccount;
import com.example.payment.repository.PaymentAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentAccountRepository paymentAccountRepository;

    @Autowired
    public PaymentServiceImpl(PaymentAccountRepository paymentAccountRepository) {
        this.paymentAccountRepository = paymentAccountRepository;
    }

    @Override
    public Mono<BigDecimal> getBalance() {
        return paymentAccountRepository.findFirstByOrderByIdAsc()
                .map(PaymentAccount::getBalance)
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    @Override
    public Mono<Boolean> processPayment(BigDecimal amount) {
        return paymentAccountRepository.findFirstByOrderByIdAsc()
                .flatMap(account -> {
                    BigDecimal newBalance = account.getBalance().subtract(amount);
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        return Mono.just(false);
                    }
                    account.setBalance(newBalance);
                    return paymentAccountRepository.save(account)
                            .thenReturn(true);
                })
                .defaultIfEmpty(false);
    }
} 