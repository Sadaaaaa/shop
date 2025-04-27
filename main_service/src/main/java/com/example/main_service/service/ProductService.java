package com.example.main_service.service;

import com.example.main_service.model.Product;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public interface ProductService {
    Flux<Product> getAllProducts();

    Mono<Product> saveProduct(Product product);

    Mono<Void> deleteProduct(Long id);

    Mono<Page<Product>> findProductsByNameOrDescription(String search, int page, int size, String sort);

    Mono<Product> findProductById(Long id);

    Mono<ByteBuffer> findProductImageById(Long id);

}
