package com.example.main_service.service;

import com.example.main_service.TestData;
import com.example.main_service.model.Product;
import com.example.main_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisConnection redisConnection;

    private ProductServiceImpl productService;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        
        // Использую lenient() для моков, которые могут не использоваться в некоторых тестах
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        lenient().when(connectionFactory.getConnection()).thenReturn(redisConnection);
        
        productService = new ProductServiceImpl(productRepository, databaseClient, redisTemplate);
    }

    @Test
    void getAllProducts_ShouldReturnProducts() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findAllProducts()).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.getAllProducts())
                .expectNext(testProduct)
                .verifyComplete();

        verify(valueOperations).set(eq("products:list"), any(), anyLong(), any());
    }

    @Test
    void findProductById_ShouldReturnProduct() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.findProductById(1L))
                .expectNext(testProduct)
                .verifyComplete();

        verify(valueOperations).set(eq("product:1"), any(), anyLong(), any());
    }

    @Test
    void findProductsByNameOrDescription_ShouldReturnFilteredProducts() {
        List<Product> products = List.of(testProduct);
        PageImpl<Product> expectedPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(Flux.fromIterable(products));

        StepVerifier.create(productService.findProductsByNameOrDescription("test", 0, 10, "name"))
                .expectNextMatches(page -> {
                    // Более точное сравнение полей страницы
                    return page.getContent().size() == expectedPage.getContent().size() && 
                           page.getTotalElements() == expectedPage.getTotalElements() &&
                           page.getPageable().getPageSize() == expectedPage.getPageable().getPageSize() &&
                           page.getPageable().getPageNumber() == expectedPage.getPageable().getPageNumber();
                })
                .verifyComplete();
    }

    @Test
    void saveProduct_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(testProduct));
        // Не используем ненужные заглушки
        // when(productRepository.findById(anyLong())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.saveProduct(testProduct))
                .expectNext(testProduct)
                .verifyComplete();

        verify(redisConnection).flushDb();
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() {
        // Не используем ненужные заглушки
        // when(productRepository.findById(anyLong())).thenReturn(Mono.just(testProduct));
        when(productRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteProduct(1L))
                .verifyComplete();

        verify(productRepository).deleteById(1L);
        verify(redisConnection).flushDb();
    }
}