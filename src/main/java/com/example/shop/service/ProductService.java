package com.example.shop.service;

import com.example.shop.model.Product;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
    Flux<Product> getAllProducts();
    Mono<Product> getProductById(Long id);
    Flux<Product> searchProducts(String name, String description);
    Flux<Product> filterProductsByPrice(Double minPrice, Double maxPrice);
    Flux<Product> findWithFilters(String name, Double minPrice, Double maxPrice);
    Mono<Product> saveProduct(Product product);
    Mono<Void> deleteProduct(Long id);
    
    Mono<Page<Product>> findProductsByNameOrDescription(String search, int page, int size, String sort);
    Mono<Product> findProductById(Long id);
}
