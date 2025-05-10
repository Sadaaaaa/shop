package com.example.main_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class OAuth2ClientConfig {

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        
        log.debug("Создание ReactiveOAuth2AuthorizedClientManager");
        
        // Создаем сервис для хранения авторизованных клиентов
        ReactiveOAuth2AuthorizedClientService clientService = 
                new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
        
        // Создаем провайдер для получения токенов client_credentials
        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = 
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();
        
        // Создаем менеджер авторизованных клиентов
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager = 
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, clientService);
        
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        
        return manager;
    }

    @Bean
    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        log.debug("Создание WebClient с поддержкой OAuth2");
        
        // Создаем фильтр для добавления токенов к запросам
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = 
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        
        // Устанавливаем идентификатор регистрации клиента по умолчанию
        oauth2Client.setDefaultClientRegistrationId("shop-client");
        
        // Создаем WebClient с OAuth2 фильтром и логированием
        return WebClient.builder()
                .filter(oauth2Client)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    /**
     * Фильтр для логирования запросов
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("OAuth2WebClient - Запрос: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> 
                values.forEach(value -> log.debug("OAuth2WebClient - Заголовок запроса: {}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    /**
     * Фильтр для логирования ответов
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("OAuth2WebClient - Ответ с кодом: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
} 