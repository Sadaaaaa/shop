package com.example.main_service;

import com.example.main_service.model.Order;
import com.example.main_service.model.OrderItem;
import com.example.main_service.model.Product;

import java.time.LocalDateTime;

public class TestData {
    public static final Long TEST_USER_ID = 1L;
    public static final Long TEST_PRODUCT_ID = 1L;
    public static final Long TEST_ORDER_ID = 1L;
    public static final double TEST_PRICE = 100.00;

    public static Product createTestProduct() {
        Product product = new Product();
        product.setId(TEST_PRODUCT_ID);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(TEST_PRICE);
        return product;
    }

    public static Order createTestOrder() {
        Order order = new Order();
        order.setId(TEST_ORDER_ID);
        order.setUserId(TEST_USER_ID);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("NEW");
        order.setTotalAmount(TEST_PRICE);
        return order;
    }

    public static OrderItem createTestOrderItem() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrderId(TEST_ORDER_ID);
        item.setProductId(TEST_PRODUCT_ID);
        item.setQuantity(1);
        item.setPrice(TEST_PRICE);
        return item;
    }
}
