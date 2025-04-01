package com.example.shop.controller;

import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import com.example.shop.model.Order;
import com.example.shop.model.OrderItem;
import com.example.shop.service.CartService;
import com.example.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    private static final Long MOCK_USER_ID = 1L; // Временное решение для демонстрации

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "orders";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        return "order";
    }

    @PostMapping("/create")
    public String createOrder() {
        Cart cart = cartService.getCart(MOCK_USER_ID);
        List<OrderItem> orderItems = new ArrayList<>();
        
        // Создаем копию списка товаров корзины
        List<CartItem> cartItems = new ArrayList<>(cart.getItems());
        
        cartItems.forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItems.add(orderItem);
        });

        Order order = orderService.createOrder(orderItems);
        // Очищаем корзину после создания заказа
        cartItems.forEach(item -> 
            cartService.removeFromCart(MOCK_USER_ID, item.getProduct().getId())
        );
        return "redirect:/orders/" + order.getId();
    }
}
