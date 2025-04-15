package com.example.shop.controller;

import com.example.shop.TestData;
import com.example.shop.model.Order;
import com.example.shop.model.OrderItem;
import com.example.shop.service.CartService;
import com.example.shop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CartService cartService;

    private Order testOrder;
    private List<OrderItem> testOrderItems;

    @BeforeEach
    void setUp() {
        testOrder = TestData.createTestOrder();
        testOrderItems = List.of(TestData.createTestOrderItem());
    }

    @Test
    void getAllOrders_ShouldReturnOrders() {
        when(orderService.getAllOrders(TestData.TEST_USER_ID))
                .thenReturn(TestData.createTestOrdersFlux());

        webTestClient.get()
                .uri("/api/orders/{userId}", TestData.TEST_USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Order.class)
                .hasSize(1)
                .contains(testOrder);
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        when(orderService.getOrderById(TestData.TEST_USER_ID, TestData.TEST_ORDER_ID))
                .thenReturn(TestData.createTestOrderMono());

        webTestClient.get()
                .uri("/api/orders/{userId}/{orderId}", TestData.TEST_USER_ID, TestData.TEST_ORDER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class)
                .isEqualTo(testOrder);
    }

    @Test
    void createOrder_ShouldCreateOrder() {
        when(orderService.createOrder(TestData.TEST_USER_ID, testOrderItems))
                .thenReturn(TestData.createTestOrderMono());

        webTestClient.post()
                .uri("/api/orders/{userId}", TestData.TEST_USER_ID)
                .bodyValue(testOrderItems)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class)
                .isEqualTo(testOrder);
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus() {
        when(orderService.updateOrderStatus(TestData.TEST_USER_ID, TestData.TEST_ORDER_ID, "PROCESSING"))
                .thenReturn(TestData.createTestOrderMono());

        webTestClient.put()
                .uri("/api/orders/{userId}/{orderId}/status?status=PROCESSING", 
                    TestData.TEST_USER_ID, TestData.TEST_ORDER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class)
                .isEqualTo(testOrder);
    }

    @Test
    void deleteOrder_ShouldDeleteOrder() {
        when(orderService.deleteOrder(TestData.TEST_USER_ID, TestData.TEST_ORDER_ID))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/orders/{userId}/{orderId}", TestData.TEST_USER_ID, TestData.TEST_ORDER_ID)
                .exchange()
                .expectStatus().isOk();
    }
} 