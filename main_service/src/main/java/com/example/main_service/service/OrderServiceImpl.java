package com.example.main_service.service;

import com.example.main_service.model.Order;
import com.example.main_service.model.OrderItem;
import com.example.main_service.repository.OrderItemRepository;
import com.example.main_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
    }

    @Override
    public Flux<Order> getAllOrders(Long userId) {
        return orderRepository.findByUserId(userId)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .map(items -> {
                            order.setItems(items);
                            return order;
                        }));
    }

    @Override
    public Mono<Order> getOrderById(Long userId, Long orderId) {
        return orderRepository.findById(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .flatMap(order -> orderItemRepository.findByOrderId(orderId)
                        .flatMap(item -> productService.findProductById(item.getProductId())
                                .map(product -> {
                                    item.setProduct(product);
                                    return item;
                                })
                                .defaultIfEmpty(item))
                        .collectList()
                        .map(items -> {
                            order.setItems(items);
                            return order;
                        }));
    }

    @Override
    @Transactional
    public Mono<Order> createOrder(Long userId, List<OrderItem> items) {
        Order order = new Order();
        order.setUserId(userId);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PENDING");

        double totalAmount = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    items.forEach(item -> item.setOrderId(savedOrder.getId()));
                    return orderItemRepository.saveAll(items)
                            .then(Mono.just(savedOrder));
                });
    }
}
