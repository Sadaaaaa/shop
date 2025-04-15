package com.example.shop.service;

import com.example.shop.model.Product;
import com.example.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Flux<Product> getAllProducts() {
        return productRepository.findAllProducts();
    }

    @Override
    public Mono<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Flux<Product> searchProducts(String name, String description) {
        if (name != null && description != null) {
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(name, description);
        } else if (name != null) {
            return productRepository.findByNameContainingIgnoreCase(name);
        }
        return Flux.empty();
    }

    @Override
    public Flux<Product> filterProductsByPrice(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    public Flux<Product> findWithFilters(String name, Double minPrice, Double maxPrice) {
        return productRepository.findWithFilters(name, minPrice, maxPrice);
    }

    @Override
    public Mono<Product> saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Mono<Void> deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }

    @Override
    public Mono<Page<Product>> findProductsByNameOrDescription(String search, int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.ASC;
        String property = "name";

        if (sort != null) {
            switch (sort) {
                case "name_desc" -> {
                    direction = Sort.Direction.DESC;
                    property = "name";
                }
                case "price_asc" -> {
                    direction = Sort.Direction.ASC;
                    property = "price";
                }
                case "price_desc" -> {
                    direction = Sort.Direction.DESC;
                    property = "price";
                }
            }
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, property));

        Flux<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        } else {
            products = productRepository.findAllProducts();
        }

        return products.collectList()
                .zipWith(productRepository.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }

    @Override
    public Mono<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }
}
