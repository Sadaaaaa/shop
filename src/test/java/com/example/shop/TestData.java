package com.example.shop;

import com.example.shop.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestData {
    public static final Long TEST_USER_ID = 1L;
    public static final Long TEST_PRODUCT_ID = 1L;
    public static final Long TEST_CART_ID = 1L;
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

    public static Cart createTestCart() {
        Cart cart = new Cart();
        cart.setId(TEST_CART_ID);
        cart.setUserId(TEST_USER_ID);
        return cart;
    }

    public static CartItem createTestCartItem() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setCartId(TEST_CART_ID);
        item.setProductId(TEST_PRODUCT_ID);
        item.setQuantity(1);
        item.setPrice(TEST_PRICE);
        return item;
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

    public static Mono<Product> createTestProductMono() {
        return Mono.just(createTestProduct());
    }

    public static Mono<Cart> createTestCartMono() {
        return Mono.just(createTestCart());
    }

    public static Mono<Order> createTestOrderMono() {
        return Mono.just(createTestOrder());
    }

    public static Flux<Product> createTestProductsFlux() {
        return Flux.just(createTestProduct());
    }

    public static Flux<Order> createTestOrdersFlux() {
        return Flux.just(createTestOrder());
    }

    public static List<Product> createTestProducts(int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Product product = new Product();
            product.setId((long) (i + 1));
            product.setName("Test Product " + (i + 1));
            product.setDescription("Test Description " + (i + 1));
            product.setPrice(TEST_PRICE * (i + 1));
            products.add(product);
        }
        return products;
    }
} 