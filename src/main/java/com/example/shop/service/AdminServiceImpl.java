package com.example.shop.service;

import com.example.shop.model.Order;
import com.example.shop.model.Product;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public Flux<Order> getAllOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Mono<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Mono<Order> updateOrderStatus(Long id, String status) {
        return orderRepository.findById(id)
                .flatMap(order -> {
                    order.setStatus(status);
                    return orderRepository.save(order);
                });
    }

    @Override
    public Mono<Void> deleteOrder(Long id) {
        return orderRepository.deleteById(id);
    }

    @Override
    public Flux<Product> getAllProducts() {
        return productRepository.findAllProducts();
    }

    @Override
    public Mono<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Mono<Product> updateProduct(Long id, Product product) {
        return productRepository.findById(id)
                .flatMap(existingProduct -> {
                    product.setId(id);
                    return productRepository.save(product);
                });
    }

    @Override
    public Mono<Void> deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }
} 