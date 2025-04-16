package com.example.shop.controller;

import com.example.shop.model.OrderItem;
import com.example.shop.service.CartService;
import com.example.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Long MOCK_USER_ID = 1L; // TODO: to_reviewer: я хотел убрать в сервис, но тогда неочевидно, что пользователь должен приходить в контроллер с фронта
    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping
    public Mono<String> listOrders(Model model) {
        return orderService.getAllOrders(MOCK_USER_ID)
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                });
    }

    @GetMapping("/{id}")
    public Mono<String> viewOrder(@PathVariable Long id, Model model) {
        return orderService.getOrderById(MOCK_USER_ID, id)
                .map(order -> {
                    model.addAttribute("order", order);
                    return "order";
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")));
    }

    @PostMapping("/create")
    public Mono<String> createOrder() {
        return cartService.getCart(MOCK_USER_ID)
                .flatMap(cart -> {
                    List<OrderItem> orderItems = new ArrayList<>();
                    cart.getItems().forEach(cartItem -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setProductId(cartItem.getProductId());
                        orderItem.setQuantity(cartItem.getQuantity());
                        orderItem.setPrice(cartItem.getPrice());
                        orderItems.add(orderItem);
                    });

                    return orderService.createOrder(MOCK_USER_ID, orderItems)
                            .flatMap(order -> {
                                List<Mono<Void>> removeOperations = new ArrayList<>();
                                cart.getItems().forEach(item ->
                                        removeOperations.add(cartService.removeItemFromCart(MOCK_USER_ID, item.getProductId()).then())
                                );
                                return Mono.when(removeOperations)
                                        .thenReturn("redirect:/orders/" + order.getId());
                            });
                });
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleOrderNotFoundException(RuntimeException ex) {
        return "error/404";
    }
}
