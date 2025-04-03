package com.example.shop.service;

import com.example.shop.model.Order;
import com.example.shop.model.OrderItem;

import java.util.List;


public interface OrderService {
    List<Order> getAllOrders();

    Order getOrderById(Long id);

    Order createOrder(Long userId, List<OrderItem> items);
}
