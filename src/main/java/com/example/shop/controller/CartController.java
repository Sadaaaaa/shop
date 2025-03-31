package com.example.shop.controller;

import com.example.shop.model.Cart;
import com.example.shop.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private static final Long MOCK_USER = 1L;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String getCartPage(Model model) {
        model.addAttribute("cart", cartService.getCart(MOCK_USER));
        return "cart";
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getCart() {
        return ResponseEntity.ok(cartService.getCartItems(MOCK_USER));
    }

    @GetMapping("/counter")
    @ResponseBody
    public ResponseEntity<?> getCartCounter() {
        return ResponseEntity.ok(cartService.getCartCounter(MOCK_USER));
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<?> getProductsCounter(Long productId) {
        return ResponseEntity.ok(cartService.getProductsCounter(MOCK_USER, productId));
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Cart> addToCart(Long productId) {
        return ResponseEntity.ok().body(cartService.addToCart(MOCK_USER, productId));
    }

    @PostMapping("/decrease")
    @ResponseBody
    public ResponseEntity<?> decreaseItems(Long productId) {
        return ResponseEntity.ok().body(cartService.decreaseItems(MOCK_USER, productId));
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Void> removeFromCart(Long productId) {
        cartService.removeFromCart(MOCK_USER, productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(@RequestBody UpdateQuantityRequest request) {
        return ResponseEntity.ok().body(cartService.updateQuantity(MOCK_USER, request.productId(), request.quantity()));
    }

    private record UpdateQuantityRequest(Long productId, Integer quantity) {}
}
