package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ProductService productService;

    @GetMapping
    public Mono<String> adminPanel(Model model) {
        return productService.getAllProducts()
                .collectList()
                .map(products -> {
                    model.addAttribute("products", products);
                    model.addAttribute("newProduct", new Product());
                    return "admin/panel";
                });
    }

    @PostMapping("/products/add")
    public Mono<String> addProduct(@RequestParam("name") String name,
                                 @RequestParam("description") String description,
                                 @RequestParam("price") double price,
                                 @RequestParam("image") FilePart image,
                                 Model model) {
        return image.content()
                .collectList()
                .flatMap(dataBuffers -> {
                    byte[] imageBytes = new byte[dataBuffers.stream().mapToInt(d -> d.readableByteCount()).sum()];
                    int offset = 0;
                    for (var buffer : dataBuffers) {
                        buffer.read(imageBytes, offset, buffer.readableByteCount());
                        offset += buffer.readableByteCount();
                    }

                    Product product = Product.builder()
                            .name(name)
                            .description(description)
                            .price(price)
                            .image(imageBytes)
                            .build();

                    return productService.saveProduct(product)
                            .thenReturn("redirect:/admin");
                })
                .onErrorResume(e -> {
                    model.addAttribute("error", "Ошибка загрузки изображения: " + e.getMessage());
                    return Mono.just("admin/panel");
                });
    }

    @PostMapping("/products/delete/{id}")
    public Mono<String> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id)
                .thenReturn("redirect:/admin");
    }
} 