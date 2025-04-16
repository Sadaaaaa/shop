package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import com.example.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Map;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ProductController {

    private static final Long MOCK_USER = 1L;
    private final ProductService productService;
    private final CartService cartService;

    @GetMapping
    public Mono<String> showProducts(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String sort,
                                   @RequestParam(required = false) String search,
                                   Model model) {
        return productService.findProductsByNameOrDescription(search, page, size, sort)
                .flatMap(products -> cartService.getCart(MOCK_USER)
                        .map(cart -> {
                            model.addAttribute("products", products.getContent());
                            model.addAttribute("totalPages", products.getTotalPages());
                            model.addAttribute("currentPage", page);
                            model.addAttribute("cart", cart);
                            model.addAttribute("param", Map.of(
                                    "search", search == null ? "" : search,
                                    "sort", sort == null ? "" : sort,
                                    "size", size
                            ));
                            return "products";
                        }));
    }

    @GetMapping("/products/{id}")
    public Mono<String> viewProduct(@PathVariable Long id, Model model) {
        return productService.findProductById(id)
                .flatMap(product -> cartService.getCart(MOCK_USER)
                        .map(cart -> {
                            model.addAttribute("product", product);
                            model.addAttribute("cart", cart);
                            return "item";
                        }));
    }

    @GetMapping(value = "/products/image/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<ResponseEntity<byte[]>> getProductImage(@PathVariable Long id) {
        return productService.findProductImageById(id)
                .filter(buffer -> buffer != null)
                .map(buffer -> {
                    ByteBuffer duplicate = buffer.duplicate();
                    duplicate.rewind();

                    byte[] bytes = new byte[duplicate.remaining()];
                    duplicate.get(bytes);

                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(bytes);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    System.err.println("Error retrieving image: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}
