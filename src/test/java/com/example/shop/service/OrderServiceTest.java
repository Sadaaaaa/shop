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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        
        OrderItem orderItem = TestData.createTestOrderItem(testProduct);
        testOrderItems.add(orderItem);
        
        testCart.getItems().add(TestData.createTestCartItem(testProduct));
        
        when(cartService.getCart(TEST_USER_ID)).thenReturn(testCart);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        when(orderRepository.findByUserId(TEST_USER_ID)).thenReturn(Arrays.asList(testOrder));
    }

    @Test
    void createOrder_ValidOrderItems_ShouldCreateOrder() {
        Order result = orderService.createOrder(TEST_USER_ID, testOrderItems);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrderById_ExistingOrder_ShouldReturnOrder() {
        Order result = orderService.getOrderById(testOrder.getId());

        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
    }

    @Test
    void getAllOrders_ShouldReturnOrdersList() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));
        List<Order> results = orderService.getAllOrders();

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testOrder.getId(), results.get(0).getId());
    }

    @Test
    void calculateOrderTotal_ValidOrderItems_ShouldReturnCorrectTotal() {
        Order result = orderService.createOrder(TEST_USER_ID, testOrderItems);
        assertNotNull(result.getTotalAmount());
        double expectedTotal = testProduct.getPrice() * testOrderItems.get(0).getQuantity();

        assertEquals(expectedTotal, result.getTotalAmount(), 0.01);
    }
} 