package com.example.shop.service;

import com.example.shop.TestData;
import com.example.shop.model.Product;
import com.example.shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
    }

    @Test
    void getAllProducts_ShouldReturnProducts() {
        when(productRepository.findAllProducts()).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.getAllProducts())
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.getProductById(1L))
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void searchProducts_ShouldReturnFilteredProducts() {
        when(productRepository.findByNameContainingIgnoreCase("test")).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.searchProducts("test", null))
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void filterProductsByPrice_ShouldReturnFilteredProducts() {
        when(productRepository.findByPriceBetween(10.0, 20.0)).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.filterProductsByPrice(10.0, 20.0))
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void findWithFilters_ShouldReturnFilteredProducts() {
        when(productRepository.findWithFilters("test", 10.0, 20.0)).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.findWithFilters("test", 10.0, 20.0))
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void saveProduct_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.saveProduct(testProduct))
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() {
        when(productRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteProduct(1L))
                .verifyComplete();
    }
} 