package com.example.main_service.service;

import com.example.main_service.TestData;
import com.example.main_service.config.TestConfig;
import com.example.main_service.model.Order;
import com.example.main_service.model.OrderItem;
import com.example.main_service.repository.OrderItemRepository;
import com.example.main_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderService orderService;

    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testOrder = TestData.createTestOrder();
        testOrderItem = TestData.createTestOrderItem();

        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(testOrder));
        when(orderItemRepository.saveAll(any(List.class))).thenReturn(Flux.just(testOrderItem));
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Flux.just(testOrderItem));
    }

    @Test
    void createOrder_ValidOrderItems_ShouldCreateOrder() {
        List<OrderItem> orderItems = List.of(testOrderItem);

        StepVerifier.create(orderService.createOrder(1L, orderItems))
                .expectNext(testOrder)
                .verifyComplete();
    }

    @Test
    void getOrderById_ExistingOrder_ShouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderService.getOrderById(1L, 1L))
                .expectNext(testOrder)
                .verifyComplete();
    }

    @Test
    void getAllOrders_ExistingOrders_ShouldReturnOrders() {
        when(orderRepository.findByUserId(1L)).thenReturn(Flux.just(testOrder));

        StepVerifier.create(orderService.getAllOrders(1L))
                .expectNext(testOrder)
                .verifyComplete();
    }

    @Test
    void calculateOrderTotal_ValidOrderItems_ShouldReturnCorrectTotal() {
        List<OrderItem> orderItems = List.of(testOrderItem);

        StepVerifier.create(orderService.createOrder(1L, orderItems))
                .expectNextMatches(order -> order.getTotalAmount() == TestData.TEST_PRICE)
                .verifyComplete();
    }
} 