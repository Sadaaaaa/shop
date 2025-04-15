package com.example.shop.service;

import com.example.shop.BaseTest;
import com.example.shop.TestData;
import com.example.shop.model.Cart;
import com.example.shop.model.Order;
import com.example.shop.model.OrderItem;
import com.example.shop.model.Product;
import com.example.shop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest extends BaseTest {

    @Autowired
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private CartService cartService;

    private Product testProduct;
    private Cart testCart;
    private Order testOrder;
    private List<OrderItem> testOrderItems;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        testCart = TestData.createTestCart();
        testOrder = TestData.createTestOrder();
        testOrderItems = new ArrayList<>();
        
        OrderItem orderItem = TestData.createTestOrderItem();
        testOrderItems.add(orderItem);
        
        testCart.getItems().add(TestData.createTestCartItem());
        
        when(cartService.getCart(TEST_USER_ID)).thenReturn(Mono.just(testCart));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(testOrder));
        when(orderRepository.findById(testOrder.getId())).thenReturn(Mono.just(testOrder));
        when(orderRepository.findByUserId(TEST_USER_ID)).thenReturn(Flux.just(testOrder));
    }

    @Test
    void createOrder_ValidOrderItems_ShouldCreateOrder() {
        Order result = orderService.createOrder(TEST_USER_ID, testOrderItems).block();

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrderById_ExistingOrder_ShouldReturnOrder() {
        Order result = orderService.getOrderById(TEST_USER_ID, testOrder.getId()).block();

        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
    }

    @Test
    void getAllOrders_ShouldReturnOrdersList() {
        List<Order> results = orderService.getAllOrders(TEST_USER_ID).collectList().block();

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testOrder.getId(), results.get(0).getId());
    }

    @Test
    void calculateOrderTotal_ValidOrderItems_ShouldReturnCorrectTotal() {
        Order result = orderService.createOrder(TEST_USER_ID, testOrderItems).block();
        assertNotNull(result.getTotalAmount());
        double expectedTotal = testProduct.getPrice() * testOrderItems.get(0).getQuantity();

        assertEquals(expectedTotal, result.getTotalAmount(), 0.01);
    }
} 