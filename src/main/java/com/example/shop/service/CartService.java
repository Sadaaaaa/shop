package com.example.shop.service;

import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import reactor.core.publisher.Mono;

public interface CartService {
    Mono<Cart> getCart(Long userId);

    Mono<Cart> addItemToCart(Long userId, CartItem item);

    Mono<Cart> removeItemFromCart(Long userId, Long productId);

    Mono<Cart> updateItemQuantity(Long userId, Long productId, int quantity);

    Mono<Void> clearCart(Long userId);

    Mono<Integer> getCartCounter(Long userId);

    Mono<Integer> getProductsCounter(Long userId, Long productId);

}
