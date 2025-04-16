package com.example.shop.service;

import com.example.shop.model.Order;
import com.example.shop.model.OrderItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Flux<Order> getAllOrders(Long userId);

    Mono<Order> getOrderById(Long userId, Long orderId);

    Mono<Order> createOrder(Long userId, List<OrderItem> items);

    Mono<Order> updateOrderStatus(Long userId, Long orderId, String status);

    Mono<Void> deleteOrder(Long userId, Long orderId);
}
