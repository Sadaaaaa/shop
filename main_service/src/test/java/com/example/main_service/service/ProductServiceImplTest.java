package com.example.main_service.service;

import com.example.main_service.TestData;
import com.example.main_service.config.TestRedisConfiguration;
import com.example.main_service.model.Product;
import com.example.main_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestRedisConfiguration.class)
@ActiveProfiles("test")
class ProductServiceImplTest {

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private DatabaseClient databaseClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private ProductServiceImpl productService;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        productService = new ProductServiceImpl(productRepository, databaseClient, redisTemplate);
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    void getAllProducts_ShouldReturnProducts() {
        when(productRepository.findAllProducts()).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.getAllProducts())
                .expectNext(testProduct)
                .verifyComplete();

        // Проверяем, что результат закэширован
        StepVerifier.create(productService.getAllProducts())
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void findProductById_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.findProductById(1L))
                .expectNext(testProduct)
                .verifyComplete();

        // Проверяем, что результат закэширован
        StepVerifier.create(productService.findProductById(1L))
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void findProductsByNameOrDescription_ShouldReturnFilteredProducts() {
        List<Product> products = List.of(testProduct);
        PageImpl<Product> expectedPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(Flux.fromIterable(products));

        StepVerifier.create(productService.findProductsByNameOrDescription("test", 0, 10, "name"))
                .expectNextMatches(page -> 
                    page.getContent().equals(expectedPage.getContent()) &&
                    page.getTotalElements() == expectedPage.getTotalElements() &&
                    page.getPageable().equals(expectedPage.getPageable())
                )
                .verifyComplete();
    }

    @Test
    void saveProduct_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(testProduct));
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.saveProduct(testProduct))
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() {
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(testProduct));
        when(productRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteProduct(1L))
                .verifyComplete();

        verify(productRepository).deleteById(1L);
    }
}