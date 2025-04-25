package com.example.main_service.service;

import com.example.main_service.model.Order;
import com.example.main_service.model.OrderItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Flux<Order> getAllOrders(Long userId);

    Mono<Order> getOrderById(Long userId, Long orderId);

    Mono<Order> createOrder(Long userId, List<OrderItem> items);
}
