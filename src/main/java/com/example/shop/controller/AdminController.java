package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final ProductService productService;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public Mono<String> adminPanel(Model model) {
        return productService.getAllProducts()
                .collectList()
                .map(products -> {
                    model.addAttribute("products", products);
                    model.addAttribute("newProduct", new Product());
                    return "admin/panel";
                });
    }

    @PostMapping(value = "/products/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> addProduct(@RequestPart("name") String name,
                                 @RequestPart("description") String description,
                                 @RequestPart("price") String price,
                                 @RequestPart(value = "image", required = false) FilePart image,
                                 Model model) {
        try {
            double priceValue = Double.parseDouble(price);
            
            if (image == null || image.filename().isEmpty()) {
                Product product = Product.builder()
                        .name(name)
                        .description(description)
                        .price(priceValue)
                        .build();
                
                return productService.saveProduct(product)
                        .doOnSuccess(p -> log.info("Товар успешно сохранен: {}", p.getId()))
                        .thenReturn("redirect:/admin")
                        .onErrorResume(e -> {
                            model.addAttribute("error", "Ошибка при сохранении товара: " + e.getMessage());
                            return Mono.just("admin/panel");
                        });
            }
            
            return image.content()
                    .collectList()
                    .flatMap(dataBuffers -> {
                        if (dataBuffers.isEmpty()) {
                            Product product = Product.builder()
                                    .name(name)
                                    .description(description)
                                    .price(priceValue)
                                    .build();
                            return productService.saveProduct(product);
                        }
                        
                        int totalBytes = dataBuffers.stream()
                                .mapToInt(DataBuffer::readableByteCount)
                                .sum();

                        byte[] imageBytes = new byte[totalBytes];
                        int offset = 0;
                        for (var buffer : dataBuffers) {
                            int length = buffer.readableByteCount();
                            buffer.read(imageBytes, offset, length);
                            offset += length;
                        }

                        Product product = Product.builder()
                                .name(name)
                                .description(description)
                                .price(priceValue)
                                .image(imageBytes)
                                .build();

                        return productService.saveProduct(product)
                                .doOnSuccess(p -> log.info("Товар с изображением успешно сохранен: {}", p.getId()));
                    })
                    .thenReturn("redirect:/admin")
                    .onErrorResume(e -> {
                        model.addAttribute("error", "Ошибка загрузки изображения: " + e.getMessage());
                        return Mono.just("admin/panel");
                    });
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Некорректное значение цены: " + price);
            return Mono.just("admin/panel");
        }
    }

    @PostMapping(value = "/products/delete/{id}", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public Mono<String> deleteProduct(@PathVariable Long id, Model model) {
        return productService.deleteProduct(id)
                .doOnSuccess(v -> log.info("Товар успешно удален: {}", id))
                .thenReturn("redirect:/admin")
                .onErrorResume(e -> {
                    if (e instanceof DataIntegrityViolationException) {
                        model.addAttribute("error", "Невозможно удалить товар, так как он используется в заказах");
                    } else {
                        model.addAttribute("error", "Ошибка при удалении товара: " + e.getMessage());
                    }

                    return productService.getAllProducts()
                            .collectList()
                            .map(products -> {
                                model.addAttribute("products", products);
                                return "admin/panel";
                            });
                });
    }
} 