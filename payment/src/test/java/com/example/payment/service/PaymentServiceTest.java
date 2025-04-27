package com.example.payment.service;

import com.example.payment.model.PaymentAccount;
import com.example.payment.repository.PaymentAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentAccountRepository paymentAccountRepository;

    private PaymentService paymentService;
    private PaymentAccount testAccount;
    private static final Long USER_ID = 1L;
    private static final Double INITIAL_AMOUNT = 1000.0;

    @BeforeEach
    void setUp() {
        testAccount = new PaymentAccount();
        testAccount.setId(1L);
        testAccount.setUserId(USER_ID);
        testAccount.setAmount(INITIAL_AMOUNT);

        paymentService = new PaymentService(paymentAccountRepository);
    }

    @Test
    void getBalance_WhenAccountExists_ShouldReturnAccount() {
        when(paymentAccountRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testAccount));

        StepVerifier.create(paymentService.getBalance(USER_ID))
                .expectNext(testAccount)
                .verifyComplete();
    }

    @Test
    void getBalance_WhenAccountDoesNotExist_ShouldReturnError() {
        when(paymentAccountRepository.findByUserId(USER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(paymentService.getBalance(USER_ID))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void processPayment_WhenAccountExistsAndHasSufficientFunds_ShouldProcessPayment() {
        Double paymentAmount = 500.0;
        Double expectedRemainingAmount = INITIAL_AMOUNT - paymentAmount;
        
        PaymentAccount updatedAccount = new PaymentAccount();
        updatedAccount.setId(1L);
        updatedAccount.setUserId(USER_ID);
        updatedAccount.setAmount(expectedRemainingAmount);
        
        when(paymentAccountRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testAccount));
        when(paymentAccountRepository.save(any(PaymentAccount.class))).thenReturn(Mono.just(updatedAccount));

        StepVerifier.create(paymentService.processPayment(USER_ID, paymentAmount))
                .expectNext(true)
                .verifyComplete();
        
        verify(paymentAccountRepository).save(any(PaymentAccount.class));
    }

    @Test
    void processPayment_WhenAccountExistsButHasInsufficientFunds_ShouldReturnError() {
        Double paymentAmount = 1500.0;
        
        when(paymentAccountRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testAccount));

        StepVerifier.create(paymentService.processPayment(USER_ID, paymentAmount))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void processPayment_WhenAccountDoesNotExist_ShouldReturnError() {
        Double paymentAmount = 500.0;
        
        when(paymentAccountRepository.findByUserId(USER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(paymentService.processPayment(USER_ID, paymentAmount))
                .expectError(RuntimeException.class)
                .verify();
    }
} 