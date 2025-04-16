package com.example.shop.controller;

import com.example.shop.TestData;
import com.example.shop.model.Cart;
import com.example.shop.model.Order;
import com.example.shop.model.OrderItem;
import com.example.shop.service.CartService;
import com.example.shop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = OrderController.class)
@Import({OrderController.class, OrderControllerTest.TestConfig.class})
class OrderControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public SpringWebFluxTemplateEngine templateEngine() {
            return new SpringWebFluxTemplateEngine();
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CartService cartService;

    private Order testOrder;
    private List<OrderItem> testOrderItems;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testOrder = TestData.createTestOrder();
        testOrderItems = List.of(TestData.createTestOrderItem());
        testCart = TestData.createTestCart();

        when(orderService.getAllOrders(TestData.TEST_USER_ID))
                .thenReturn(Flux.just(testOrder));
        when(orderService.getOrderById(TestData.TEST_USER_ID, TestData.TEST_ORDER_ID))
                .thenReturn(Mono.just(testOrder));
        when(orderService.createOrder(eq(TestData.TEST_USER_ID), anyList()))
                .thenReturn(Mono.just(testOrder));
        when(orderService.updateOrderStatus(TestData.TEST_USER_ID, TestData.TEST_ORDER_ID, "PROCESSING"))
                .thenReturn(Mono.just(testOrder));
        when(orderService.deleteOrder(TestData.TEST_USER_ID, TestData.TEST_ORDER_ID))
                .thenReturn(Mono.empty());
        when(cartService.getCart(TestData.TEST_USER_ID))
                .thenReturn(Mono.just(testCart));
        when(cartService.removeItemFromCart(anyLong(), anyLong()))
                .thenReturn(Mono.empty());
    }

    @Test
    void listOrders_ShouldReturnOrdersPage() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void viewOrder_ShouldReturnOrderPage() {
        webTestClient.get()
                .uri("/orders/{id}", TestData.TEST_ORDER_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void createOrder_ShouldCreateOrderAndRedirect() {
        webTestClient.post()
                .uri("/orders/create")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/" + TestData.TEST_ORDER_ID);

        verify(cartService).getCart(TestData.TEST_USER_ID);
        verify(orderService).createOrder(eq(TestData.TEST_USER_ID), anyList());
        verify(cartService, times(testCart.getItems().size())).removeItemFromCart(anyLong(), anyLong());
    }
} 