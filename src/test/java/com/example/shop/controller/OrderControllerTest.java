package com.example.shop.controller;

import com.example.shop.BaseTest;
import com.example.shop.TestData;
import com.example.shop.model.Cart;
import com.example.shop.model.Order;
import com.example.shop.service.CartService;
import com.example.shop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
class OrderControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CartService cartService;

    private Order testOrder;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testOrder = TestData.createTestOrder();
        testCart = TestData.createTestCart();
        testCart.getItems().add(TestData.createTestCartItem(TestData.createTestProduct()));

        when(cartService.getCart(any())).thenReturn(testCart);
        when(orderService.createOrder(anyLong(), any())).thenReturn(testOrder);
        when(orderService.getOrderById(testOrder.getId())).thenReturn(testOrder);
        when(orderService.getAllOrders()).thenReturn(Arrays.asList(testOrder));
    }

    @Test
    void createOrder_ValidRequest_ShouldRedirectToOrder() throws Exception {
        mockMvc.perform(post("/orders/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*"));

        verify(orderService).createOrder(anyLong(), any());
    }

    @Test
    void viewOrder_ExistingOrder_ShouldReturnOrderPage() throws Exception {
        mockMvc.perform(get("/orders/{id}", testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"));

        verify(orderService).getOrderById(testOrder.getId());
    }

    @Test
    void listOrders_ShouldReturnOrdersPage() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));

        verify(orderService).getAllOrders();
    }

    @Test
    void viewOrder_NonExistentOrder_ShouldThrowException() throws Exception {
        when(orderService.getOrderById(anyLong())).thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }
} 