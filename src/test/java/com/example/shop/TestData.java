package com.example.shop;

import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import com.example.shop.model.Order;
import com.example.shop.model.OrderItem;
import com.example.shop.model.Product;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestData {

    public static Product createTestProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Тестовый продукт");
        product.setDescription("Описание тестового продукта");
        product.setPrice(999.99);
        return product;
    }

    public static Cart createTestCart() {
        Cart cart = new Cart();
        cart.setUserId(1L);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    public static CartItem createTestCartItem(Product product) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        return item;
    }

    public static Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(999.99);
        order.setOrderItems(new ArrayList<>());
        return order;
    }

    public static OrderItem createTestOrderItem(Product product) {
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(product.getPrice());
        return item;
    }

    public static List<Product> createTestProducts(int count) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Product product = new Product();
            product.setId((long) (i + 1));
            product.setName("Продукт " + (i + 1));
            product.setDescription("Описание продукта " + (i + 1));
            product.setPrice((i + 1) * 100);
            products.add(product);
        }
        return products;
    }
} 