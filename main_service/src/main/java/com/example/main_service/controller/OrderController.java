package com.example.main_service.controller;

import com.example.main_service.model.OrderItem;
import com.example.main_service.service.CartService;
import com.example.main_service.service.OrderService;
import com.example.main_service.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Validated
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public Mono<String> listOrders(Model model) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> orderService.getAllOrders(userId)
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                }));
    }

    @GetMapping("/{id}")
    public Mono<String> viewOrder(@PathVariable @NotNull Long id, Model model) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> orderService.getOrderById(userId, id)
                .map(order -> {
                    model.addAttribute("order", order);
                    return "order";
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found"))));
    }

    @PostMapping("/create")
    public Mono<String> createOrder() {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.getCart(userId)
                .flatMap(cart -> {
                    List<OrderItem> orderItems = new ArrayList<>();
                    cart.getItems().forEach(cartItem -> {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setProductId(cartItem.getProductId());
                        orderItem.setQuantity(cartItem.getQuantity());
                        orderItem.setPrice(cartItem.getPrice());
                        orderItems.add(orderItem);
                    });

                    return orderService.createOrder(userId, orderItems)
                            .flatMap(order -> {
                                List<Mono<Void>> removeOperations = new ArrayList<>();
                                cart.getItems().forEach(item ->
                                        removeOperations.add(cartService.removeItemFromCart(userId, item.getProductId()).then())
                                );
                                return Mono.when(removeOperations)
                                        .thenReturn("redirect:/orders/" + order.getId());
                            });
                }));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleOrderNotFoundException(RuntimeException ex) {
        return "error/404";
    }
}
