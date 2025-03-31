package com.example.shop.service;

import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;

import java.util.List;
import java.util.Map;

public interface CartService {
    Cart getCart(Long userId);

    List<CartItem> getCartItems(Long userId);

    Cart addToCart(Long userId, Long productId);

    Cart decreaseItems(Long userId, Long productId);

    void removeFromCart(Long userId, Long productId);

    Integer getCartCounter(Long userId);

    Integer getProductsCounter(Long userId, Long productId);

    Map<Long, Integer> getCartItemsQuantity(Long userId);

    Cart updateQuantity(Long userId, Long productId, Integer quantity);
}
