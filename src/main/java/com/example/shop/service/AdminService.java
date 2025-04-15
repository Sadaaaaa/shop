package com.example.shop.service;

import com.example.shop.model.Order;
import com.example.shop.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminService {
    Flux<Order> getAllOrders(Long userId);
    Mono<Order> getOrderById(Long id);
    Mono<Order> updateOrderStatus(Long id, String status);
    Mono<Void> deleteOrder(Long id);
    
    Flux<Product> getAllProducts();
    Mono<Product> getProductById(Long id);
    Mono<Product> createProduct(Product product);
    Mono<Product> updateProduct(Long id, Product product);
    Mono<Void> deleteProduct(Long id);
} 