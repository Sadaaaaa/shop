package com.example.main_service.repository;

import com.example.main_service.model.CartItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {
    @Query("SELECT id, cart_id, product_id, quantity, price FROM cart_items WHERE cart_id = :cartId")
    Flux<CartItem> findByCartId(Long cartId);

    @Query("DELETE FROM cart_items WHERE cart_id = :cartId AND product_id = :productId")
    Flux<Void> deleteByCartIdAndProductId(Long cartId, Long productId);
} 