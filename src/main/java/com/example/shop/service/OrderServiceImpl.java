package com.example.shop.service;

import com.example.shop.model.Order;
import com.example.shop.model.OrderItem;
import com.example.shop.model.Product;
import com.example.shop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    @Override
    public Order createOrder(List<OrderItem> items) {
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setOrderItems(items);

        double totalAmount = 0;
        for (OrderItem item : items) {
            Product product = productService.findProductById(item.getProduct().getId());
            item.setOrder(order);
            item.setPrice(product.getPrice());
            totalAmount += product.getPrice() * item.getQuantity();
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }


}
