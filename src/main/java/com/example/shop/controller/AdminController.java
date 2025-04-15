package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping(value = "/products/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> addProduct(@RequestPart("name") String name,
                                 @RequestPart("description") String description,
                                 @RequestPart("price") String price,
                                 @RequestPart(value = "image", required = false) FilePart image,
                                 Model model) {
        log.info("Добавление продукта: {}, цена: {}", name, price);
        
        try {
            double priceValue = Double.parseDouble(price);
            
            if (image == null || image.filename().isEmpty()) {
                log.info("Изображение не предоставлено, сохраняем товар без изображения");
                Product product = Product.builder()
                        .name(name)
                        .description(description)
                        .price(priceValue)
                        .build();
                
                return productService.saveProduct(product)
                        .doOnSuccess(p -> log.info("Товар успешно сохранен: {}", p.getId()))
                        .thenReturn("redirect:/admin");
            }
            
            log.info("Начинаем загрузку изображения: {}", image.filename());
            return image.content()
                    .collectList()
                    .flatMap(dataBuffers -> {
                        if (dataBuffers.isEmpty()) {
                            log.warn("Получен пустой список буферов для изображения");
                            Product product = Product.builder()
                                    .name(name)
                                    .description(description)
                                    .price(priceValue)
                                    .build();
                            return productService.saveProduct(product);
                        }
                        
                        int totalBytes = dataBuffers.stream()
                                .mapToInt(d -> d.readableByteCount())
                                .sum();
                        
                        log.info("Размер изображения: {} байт", totalBytes);
                        
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
                        log.error("Ошибка при загрузке изображения: {}", e.getMessage(), e);
                        model.addAttribute("error", "Ошибка загрузки изображения: " + e.getMessage());
                        return productService.getAllProducts()
                                .collectList()
                                .map(products -> {
                                    model.addAttribute("products", products);
                                    return "admin/panel";
                                });
                    });
        } catch (NumberFormatException e) {
            log.error("Некорректное значение цены: {}", price, e);
            model.addAttribute("error", "Некорректное значение цены: " + price);
            return productService.getAllProducts()
                    .collectList()
                    .map(products -> {
                        model.addAttribute("products", products);
                        return "admin/panel";
                    });
        }
    }

    @PostMapping("/products/delete/{id}")
    public Mono<String> deleteProduct(@PathVariable Long id) {
        log.info("Удаление товара с ID: {}", id);
        return productService.deleteProduct(id)
                .doOnSuccess(v -> log.info("Товар успешно удален: {}", id))
                .thenReturn("redirect:/admin");
    }
} 