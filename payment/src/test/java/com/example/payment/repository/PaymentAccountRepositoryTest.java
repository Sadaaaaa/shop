package com.example.payment.repository;

import com.example.payment.model.PaymentAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@ActiveProfiles("test")
class PaymentAccountRepositoryTest {

    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    @Autowired
    private R2dbcEntityTemplate template;

    private PaymentAccount testAccount;
    private static final Long USER_ID = 1L;
    private static final Double INITIAL_AMOUNT = 1000.0;

    @BeforeEach
    void setUp() {
        Class<PaymentAccount> accountClass = PaymentAccount.class;
        StepVerifier.create(template.delete(accountClass).all())
                .expectNextCount(1)
                .verifyComplete();

        testAccount = new PaymentAccount();
        testAccount.setUserId(USER_ID);
        testAccount.setAmount(INITIAL_AMOUNT);

        StepVerifier.create(template.insert(testAccount))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByUserId_WhenAccountExists_ShouldReturnAccount() {
        StepVerifier.create(paymentAccountRepository.findByUserId(USER_ID))
                .expectNextMatches(account ->
                        account.getUserId().equals(USER_ID) &&
                                account.getAmount().equals(INITIAL_AMOUNT))
                .verifyComplete();
    }

    @Test
    void findByUserId_WhenAccountDoesNotExist_ShouldReturnEmpty() {
        Long nonExistentUserId = 999L;

        StepVerifier.create(paymentAccountRepository.findByUserId(nonExistentUserId))
                .verifyComplete();
    }

    @Test
    void save_ShouldUpdateAccount() {
        double newAmount = 500.0;

        Mono<PaymentAccount> updateOperation = paymentAccountRepository.findByUserId(USER_ID)
                .flatMap(account -> {
                    account.setAmount(newAmount);
                    return paymentAccountRepository.save(account);
                });

        StepVerifier.create(updateOperation)
                .expectNextMatches(account -> account.getAmount().equals(newAmount))
                .verifyComplete();

        StepVerifier.create(paymentAccountRepository.findByUserId(USER_ID))
                .expectNextMatches(account -> account.getAmount().equals(newAmount))
                .verifyComplete();
    }
} 