package com.example.shop.repository;

import com.example.shop.model.Cart;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface CartRepository extends R2dbcRepository<Cart, Long> {
    Mono<Cart> findByUserId(Long userId);
}