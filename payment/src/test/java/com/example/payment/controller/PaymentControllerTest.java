package com.example.payment.controller;

import com.example.payment.model.PaymentAccount;
import com.example.payment.model.dto.Balance;
import com.example.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private WebTestClient webTestClient;
    private PaymentAccount testAccount;
    private static final Long USER_ID = 1L;
    private static final Double INITIAL_AMOUNT = 1000.0;

    @BeforeEach
    void setUp() {
        testAccount = new PaymentAccount();
        testAccount.setId(1L);
        testAccount.setUserId(USER_ID);
        testAccount.setAmount(INITIAL_AMOUNT);

        webTestClient = WebTestClient.bindToController(paymentController)
                .configureClient()
                .baseUrl("")
                .build();
    }

    @Test
    void getBalance_ShouldReturnBalance() {
        when(paymentService.getBalance(anyLong())).thenReturn(Mono.just(testAccount));

        webTestClient.get()
                .uri("/payments/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Balance.class)
                .value(balance -> {
                    assert balance.getAmount().equals(testAccount.getAmount());
                });
    }

    @Test
    void getBalance_WhenErrorOccurs_ShouldReturnInternalServerError() {
        when(paymentService.getBalance(anyLong())).thenReturn(Mono.error(new RuntimeException("Account not found")));

        webTestClient.get()
                .uri("/payments/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(Balance.class)
                .value(balance -> {
                    assert balance.getAmount().equals(0.0);
                });
    }

    @Test
    void processPayment_WhenSufficientFunds_ShouldReturnTrue() {
        Double paymentAmount = 500.0;

        when(paymentService.processPayment(anyLong(), anyDouble())).thenReturn(Mono.just(true));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/process")
                        .queryParam("amount", paymentAmount)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void processPayment_WhenInsufficientFunds_ShouldReturnBadRequest() {
        Double paymentAmount = 1500.0;

        when(paymentService.processPayment(anyLong(), anyDouble()))
                .thenReturn(Mono.error(new RuntimeException("Insufficient funds")));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/process")
                        .queryParam("amount", paymentAmount)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }
} 