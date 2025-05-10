package com.example.main_service.controller;

import com.example.main_service.dto.UpdateQuantityRequest;
import com.example.main_service.model.Cart;
import com.example.main_service.model.CartItem;
import com.example.main_service.service.CartService;
import com.example.main_service.service.ProductService;
import com.example.main_service.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

@Slf4j
@Validated
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public Mono<String> getCartPage(Model model) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.getCart(userId))
                .map(cart -> {
                    model.addAttribute("cart", cart);
                    return "cart";
                });
    }

    @GetMapping("/api")
    @ResponseBody
    public Mono<Cart> getCart() {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.getCart(userId));
    }

    @GetMapping("/counter")
    @ResponseBody
    public Mono<Integer> getCartCounter() {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.getCartCounter(userId));
    }

    @GetMapping("/count")
    @ResponseBody
    public Mono<Integer> getProductsCounter(@RequestParam @NotNull Long productId) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.getProductsCounter(userId, productId));
    }

    @PostMapping("/add")
    @ResponseBody
    public Mono<Cart> addToCart(@RequestParam @NotNull Long productId) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.getCart(userId)
                .flatMap(cart -> {
                    CartItem existingItem = cart.getItems().stream()
                            .filter(item -> item.getProductId().equals(productId))
                            .findFirst()
                            .orElse(null);

                    if (existingItem != null) {
                        return cartService.updateItemQuantity(userId, productId, existingItem.getQuantity() + 1);
                    } else {
                        return productService.findProductById(productId)
                                .flatMap(product -> {
                                    CartItem item = new CartItem();
                                    item.setProductId(productId);
                                    item.setQuantity(1);
                                    item.setPrice(product.getPrice());
                                    return cartService.addItemToCart(userId, item);
                                });
                    }
                }));
    }

    @PostMapping("/decrease")
    @ResponseBody
    public Mono<Cart> decreaseItems(@RequestParam @NotNull Long productId) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.getCart(userId)
                .flatMap(cart -> {
                    CartItem item = cart.getItems().stream()
                            .filter(i -> i.getProductId().equals(productId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));

                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        return cartService.updateItemQuantity(userId, productId, item.getQuantity());
                    } else {
                        return cartService.removeItemFromCart(userId, productId);
                    }
                }));
    }

    @PostMapping("/remove")
    @ResponseBody
    public Mono<Cart> removeFromCart(@RequestParam @NotNull Long productId) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.removeItemFromCart(userId, productId));
    }

    @PostMapping("/update")
    @ResponseBody
    public Mono<Cart> updateQuantity(@RequestBody UpdateQuantityRequest request) {
        if (request.productId() == null || request.quantity() == null) {
            return Mono.error(new IllegalArgumentException("Wrong request!"));
        }

        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.updateItemQuantity(userId, request.productId(), request.quantity()));
    }

    @PostMapping("/items")
    public Mono<ResponseEntity<Cart>> addItemToCart(@RequestBody CartItem item) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.addItemToCart(userId, item))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/items/{productId}")
    public Mono<ResponseEntity<Cart>> removeItemFromCart(@PathVariable @NonNull Long productId) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.removeItemFromCart(userId, productId))
                .map(ResponseEntity::ok);
    }

    @PutMapping("/items/{productId}/quantity")
    public Mono<ResponseEntity<Cart>> updateItemQuantity(@PathVariable @NonNull Long productId,
                                                         @RequestParam int quantity) {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.updateItemQuantity(userId, productId, quantity))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping
    public Mono<ResponseEntity<Void>> clearCart() {
        return userService.getUserIdFromSecurityContext()
                .flatMap(userId -> cartService.clearCart(userId))
                .then(Mono.just(ResponseEntity.ok().build()));
    }
}
