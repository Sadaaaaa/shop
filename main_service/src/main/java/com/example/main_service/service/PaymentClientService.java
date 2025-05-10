package com.example.main_service.service;

import com.example.main_service.client.payment.model.Balance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PaymentClientService {

    private final WebClient webClient;

    public PaymentClientService(
            @Value("${payment.service.url:http://localhost:8081}") String paymentServiceUrl,
            WebClient webClient) {
        
        log.info("Инициализация PaymentClientService. URL платежного сервиса: {}", paymentServiceUrl);
        
        // Создаем веб-клиент на основе существующего OAuth2 клиента с базовым URL платежного сервиса
        this.webClient = webClient.mutate()
                .baseUrl(paymentServiceUrl)
                .build();
    }

    public Mono<Balance> getBalance(Long userId) {
        log.info("Запрос баланса для пользователя: {}", userId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/balance")
                        .queryParam("userId", userId)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Balance.class)
                .doOnNext(balance -> log.info("Получен баланс: {}", balance.getAmount()))
                .doOnError(e -> log.error("Ошибка при получении баланса для пользователя {}: {}", userId, e.getMessage()));
    }

    public Mono<Boolean> processPayment(Long userId, Double amount) {
        log.info("Запрос на списание суммы {} для пользователя: {}", amount, userId);
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/process")
                        .queryParam("userId", userId)
                        .queryParam("amount", amount)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnNext(result -> log.info("Результат обработки платежа: {}", result ? "успешно" : "отклонено"))
                .doOnError(e -> log.error("Ошибка при обработке платежа для пользователя {}: {}", userId, e.getMessage()));
    }
} 