package com.example.shop.controller;

import com.example.shop.dto.UpdateQuantityRequest;
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
    public ResponseEntity<?> getProductsCounter(@RequestParam(required = true) Long productId) {
        if (productId == null) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        return ResponseEntity.ok(cartService.getProductsCounter(MOCK_USER, productId));
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestParam(required = true) Long productId) {
        if (productId == null) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        try {
            Cart cart = cartService.addToCart(MOCK_USER, productId);
            return ResponseEntity.ok().body(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding product to cart: " + e.getMessage());
        }
    }

    @PostMapping("/decrease")
    @ResponseBody
    public ResponseEntity<?> decreaseItems(@RequestParam(required = true) Long productId) {
        if (productId == null) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        try {
            Cart cart = cartService.decreaseItems(MOCK_USER, productId);
            return ResponseEntity.ok().body(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error decreasing product quantity: " + e.getMessage());
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@RequestParam(required = true) Long productId) {
        if (productId == null) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        cartService.removeFromCart(MOCK_USER, productId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(@RequestBody UpdateQuantityRequest request) {
        if (request.productId() == null) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }
        if (request.quantity() == null) {
            return ResponseEntity.badRequest().body("Quantity is required");
        }
        try {
            Cart cart = cartService.updateQuantity(MOCK_USER, request.productId(), request.quantity());
            return ResponseEntity.ok().body(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating product quantity: " + e.getMessage());
        }
    }


}
