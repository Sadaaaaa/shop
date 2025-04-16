package com.example.shop.controller;

import com.example.shop.TestData;
import com.example.shop.model.Cart;
import com.example.shop.model.Product;
import com.example.shop.service.CartService;
import com.example.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ProductController.class)
@Import({ProductController.class, ProductControllerTest.TestConfig.class})
class ProductControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public SpringWebFluxTemplateEngine templateEngine() {
            return new SpringWebFluxTemplateEngine();
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @MockBean
    private CartService cartService;

    private Product testProduct;
    private List<Product> testProducts;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        testProducts = TestData.createTestProducts(3);
        testCart = TestData.createTestCart();

        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(testProducts));
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));
        when(productService.findProductImageById(anyLong())).thenReturn(Mono.just(ByteBuffer.allocate(0)));
        when(cartService.getCart(any())).thenReturn(Mono.just(testCart));
    }

    @Test
    void showProducts_ShouldReturnProductsPage() {
        when(productService.findProductsByNameOrDescription(isNull(), anyInt(), anyInt(), isNull()))
                .thenReturn(Mono.just(new PageImpl<>(testProducts)));

        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void viewProduct_ExistingProduct_ShouldReturnProductPage() {
        webTestClient.get()
                .uri("/products/{id}", testProduct.getId())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showProducts_WithSearchQuery_ShouldReturnFilteredProducts() {
        String keyword = "test";
        when(productService.findProductsByNameOrDescription(eq(keyword), anyInt(), anyInt(), isNull()))
                .thenReturn(Mono.just(new PageImpl<>(testProducts.subList(0, 1))));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("search", keyword)
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getProductImage_ExistingImage_ShouldReturnImage() {
        byte[] imageData = "test image content".getBytes();
        ByteBuffer imageBuffer = ByteBuffer.wrap(imageData);
        when(productService.findProductImageById(testProduct.getId())).thenReturn(Mono.just(imageBuffer));

        webTestClient.get()
                .uri("/products/image/{id}", testProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG)
                .expectBody(byte[].class).isEqualTo(imageData);
    }
} 