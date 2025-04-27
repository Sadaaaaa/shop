package com.example.main_service.controller;

import com.example.main_service.dto.UpdateQuantityRequest;
import com.example.main_service.model.Cart;
import com.example.main_service.model.CartItem;
import com.example.main_service.service.CartService;
import com.example.main_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final ProductService productService;
    private static final Long MOCK_USER = 1L;

    @GetMapping
    public Mono<String> getCartPage(Model model) {
        return cartService.getCart(MOCK_USER)
                .map(cart -> {
                    model.addAttribute("cart", cart);
                    return "cart";
                });
    }

    @GetMapping("/api")
    @ResponseBody
    public Mono<Cart> getCart() {
        return cartService.getCart(MOCK_USER);
    }

    @GetMapping("/counter")
    @ResponseBody
    public Mono<Integer> getCartCounter() {
        return cartService.getCartCounter(MOCK_USER);
    }

    @GetMapping("/count")
    @ResponseBody
    public Mono<Integer> getProductsCounter(@RequestParam Long productId) {
        if (productId == null) {
            return Mono.error(new IllegalArgumentException("Product ID is required"));
        }
        return cartService.getProductsCounter(MOCK_USER, productId);
    }

    @PostMapping("/add")
    @ResponseBody
    public Mono<Cart> addToCart(@RequestParam Long productId) {
        if (productId == null) {
            return Mono.error(new IllegalArgumentException("Product ID is required"));
        }

        return cartService.getCart(MOCK_USER)
                .flatMap(cart -> {
                    CartItem existingItem = cart.getItems().stream()
                            .filter(item -> item.getProductId().equals(productId))
                            .findFirst()
                            .orElse(null);

                    if (existingItem != null) {
                        return cartService.updateItemQuantity(MOCK_USER, productId, existingItem.getQuantity() + 1);
                    } else {
                        return productService.findProductById(productId)
                                .flatMap(product -> {
                                    CartItem item = new CartItem();
                                    item.setProductId(productId);
                                    item.setQuantity(1);
                                    item.setPrice(product.getPrice());
                                    return cartService.addItemToCart(MOCK_USER, item);
                                });
                    }
                });
    }

    @PostMapping("/decrease")
    @ResponseBody
    public Mono<Cart> decreaseItems(@RequestParam Long productId) {
        if (productId == null) {
            return Mono.error(new IllegalArgumentException("Product ID is required"));
        }
        return cartService.getCart(MOCK_USER)
                .flatMap(cart -> {
                    CartItem item = cart.getItems().stream()
                            .filter(i -> i.getProductId().equals(productId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));

                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        return cartService.updateItemQuantity(MOCK_USER, productId, item.getQuantity());
                    } else {
                        return cartService.removeItemFromCart(MOCK_USER, productId);
                    }
                });
    }

    @PostMapping("/remove")
    @ResponseBody
    public Mono<Cart> removeFromCart(@RequestParam Long productId) {
        if (productId == null) {
            return Mono.error(new IllegalArgumentException("Product ID is required"));
        }
        return cartService.removeItemFromCart(MOCK_USER, productId);
    }

    @PostMapping("/update")
    @ResponseBody
    public Mono<Cart> updateQuantity(@RequestBody UpdateQuantityRequest request) {
        if (request.productId() == null) {
            return Mono.error(new IllegalArgumentException("Product ID is required"));
        }
        if (request.quantity() == null) {
            return Mono.error(new IllegalArgumentException("Quantity is required"));
        }
        return cartService.updateItemQuantity(MOCK_USER, request.productId(), request.quantity());
    }

    @PostMapping("/items")
    public Mono<ResponseEntity<Cart>> addItemToCart(@RequestBody CartItem item) {
        return cartService.addItemToCart(MOCK_USER, item)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/items/{productId}")
    public Mono<ResponseEntity<Cart>> removeItemFromCart(@PathVariable Long productId) {
        return cartService.removeItemFromCart(MOCK_USER, productId)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/items/{productId}/quantity")
    public Mono<ResponseEntity<Cart>> updateItemQuantity(@PathVariable Long productId,
                                                         @RequestParam int quantity) {
        return cartService.updateItemQuantity(MOCK_USER, productId, quantity)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping
    public Mono<ResponseEntity<Void>> clearCart() {
        return cartService.clearCart(MOCK_USER)
                .then(Mono.just(ResponseEntity.ok().build()));
    }
}
