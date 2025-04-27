package com.example.main_service.controller;

import com.example.main_service.model.Cart;
import com.example.main_service.model.CartItem;
import com.example.main_service.model.Order;
import com.example.main_service.model.OrderItem;
import com.example.main_service.service.CartService;
import com.example.main_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @InjectMocks
    private OrderController orderController;

    private Order testOrder;
    private Cart testCart;
    private CartItem testCartItem;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testCartItem = new CartItem();
        testCartItem.setProductId(1L);
        testCartItem.setQuantity(2);
        testCartItem.setPrice(100.0);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(1L);
        testCart.setItems(new ArrayList<>(List.of(testCartItem)));

        testOrderItem = new OrderItem();
        testOrderItem.setProductId(1L);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(100.0);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setItems(List.of(testOrderItem));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setTotalAmount(200.0);
    }

    @Test
    void listOrders_ShouldReturnOrdersPage() {
        List<Order> orders = List.of(testOrder);
        when(orderService.getAllOrders(anyLong())).thenReturn(Flux.fromIterable(orders));
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        StepVerifier.create(orderController.listOrders(model))
                .expectNext("orders")
                .verifyComplete();

        verify(model).addAttribute("orders", orders);
    }

    @Test
    void viewOrder_ShouldReturnOrderPage() {
        when(orderService.getOrderById(anyLong(), anyLong())).thenReturn(Mono.just(testOrder));
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        StepVerifier.create(orderController.viewOrder(1L, model))
                .expectNext("order")
                .verifyComplete();

        verify(model).addAttribute("order", testOrder);
    }

    @Test
    void viewOrder_ShouldReturnError_WhenOrderNotFound() {
        when(orderService.getOrderById(anyLong(), anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(orderController.viewOrder(1L, model))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void createOrder_ShouldCreateOrderAndClearCart() {
        when(cartService.getCart(anyLong())).thenReturn(Mono.just(testCart));
        when(orderService.createOrder(anyLong(), anyList())).thenReturn(Mono.just(testOrder));
        when(cartService.removeItemFromCart(anyLong(), anyLong())).thenReturn(Mono.just(new Cart()));

        StepVerifier.create(orderController.createOrder())
                .expectNext("redirect:/orders/1")
                .verifyComplete();

        verify(cartService).getCart(anyLong());
        verify(orderService).createOrder(anyLong(), anyList());
        verify(cartService).removeItemFromCart(anyLong(), anyLong());
    }

    @Test
    void handleOrderNotFoundException_ShouldReturnErrorPage() {
        String result = orderController.handleOrderNotFoundException(new RuntimeException("Order not found"));
        assertEquals("error/404", result);
    }
} 