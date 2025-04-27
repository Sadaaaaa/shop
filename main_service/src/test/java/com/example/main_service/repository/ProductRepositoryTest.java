package com.example.main_service.repository;

import com.example.main_service.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private R2dbcEntityTemplate template;

    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        template.<Product>delete(Product.class).all()
                .then(Mono.defer(() -> {
                    product1 = Product.builder()
                            .name("iPhone 13")
                            .description("Apple iPhone 13")
                            .price(999.99)
                            .build();

                    product2 = Product.builder()
                            .name("Samsung Galaxy")
                            .description("Samsung Galaxy S21")
                            .price(899.99)
                            .build();

                    product3 = Product.builder()
                            .name("Pixel 6")
                            .description("Google Pixel 6")
                            .price(799.99)
                            .build();

                    return template.<Product>insert(Product.class)
                            .using(product1)
                            .then(template.<Product>insert(Product.class).using(product2))
                            .then(template.<Product>insert(Product.class).using(product3));
                }))
                .block();
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        StepVerifier.create(productRepository.findByNameContainingIgnoreCase("iPhone"))
                .expectNextMatches(product ->
                        product.getName().equals("iPhone 13") &&
                                product.getPrice() == 999.99)
                .verifyComplete();
    }

    @Test
    void findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase_ShouldReturnMatchingProducts() {
        StepVerifier.create(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("Samsung", "Samsung"))
                .expectNextMatches(product ->
                        product.getName().equals("Samsung Galaxy") &&
                                product.getDescription().contains("Samsung"))
                .verifyComplete();
    }

    @Test
    void findByPriceBetween_ShouldReturnProductsInPriceRange() {
        StepVerifier.create(productRepository.findByPriceBetween(800.0, 900.0))
                .expectNextMatches(product ->
                        product.getName().equals("Samsung Galaxy") &&
                                product.getPrice() == 899.99)
                .verifyComplete();
    }

    @Test
    void findWithFilters_ShouldReturnFilteredProducts() {
        StepVerifier.create(productRepository.findWithFilters("Samsung", 800.0, 1000.0))
                .expectNextMatches(product ->
                        product.getName().equals("Samsung Galaxy") &&
                                product.getPrice() == 899.99)
                .verifyComplete();
    }

    @Test
    void findWithFilters_ShouldReturnAllProducts_WhenNoFilters() {
        StepVerifier.create(productRepository.findWithFilters(null, null, null))
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findAllProducts_ShouldReturnAllProducts() {
        StepVerifier.create(productRepository.findAllProducts())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void save_ShouldSaveProduct() {
        Product newProduct = Product.builder()
                .name("New Product")
                .description("Test Description")
                .price(199.99)
                .build();

        StepVerifier.create(productRepository.save(newProduct))
                .expectNextMatches(saved ->
                        saved.getId() != null &&
                                saved.getName().equals("New Product") &&
                                saved.getPrice() == 199.99)
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnProduct() {
        StepVerifier.create(productRepository.findAllProducts()
                        .next()
                        .flatMap(product -> productRepository.findById(product.getId())))
                .expectNextMatches(found -> found.getId() != null)
                .verifyComplete();
    }

    @Test
    void deleteById_ShouldDeleteProduct() {
        StepVerifier.create(productRepository.findAllProducts()
                        .next()
                        .flatMap(product -> productRepository.deleteById(product.getId())
                                .then(productRepository.findById(product.getId()))))
                .verifyComplete();
    }
} 