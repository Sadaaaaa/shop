package com.example.shop.controller;

import com.example.shop.TestData;
import com.example.shop.model.Product;
import com.example.shop.model.Order;
import com.example.shop.service.ProductService;
import com.example.shop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = AdminController.class)
@Import({AdminController.class, AdminControllerTest.TestConfig.class})
class AdminControllerTest {

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
    private OrderService orderService;

    private Product testProduct;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        testProducts = TestData.createTestProducts(3);
        
        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(testProducts));
        when(productService.saveProduct(any(Product.class))).thenReturn(Mono.just(testProduct));
        when(productService.deleteProduct(anyLong())).thenReturn(Mono.empty());
        when(orderService.getAllOrders(anyLong())).thenReturn(Flux.fromIterable(List.of(TestData.createTestOrder())));
    }

    @Test
    void adminPanel_ShouldReturnAdminPage() {
        webTestClient.get()
                .uri("/admin")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void addProduct_ValidProduct_ShouldRedirectToAdmin() {
        webTestClient.post()
                .uri("/admin/products/add")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("name", "Test Product")
                        .with("description", "Test Description")
                        .with("price", "99.99"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/admin");

        verify(productService).saveProduct(any(Product.class));
    }

    @Test
    void deleteProduct_ExistingProduct_ShouldRedirectToAdmin() {
        webTestClient.post()
                .uri("/admin/products/delete/{id}", testProduct.getId())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/admin");

        verify(productService).deleteProduct(testProduct.getId());
    }

    @Test
    void listProducts_ShouldReturnProductsList() {
        webTestClient.get()
                .uri("/admin/products")
                .exchange()
                .expectStatus().isOk();

        verify(productService).getAllProducts();
    }

    @Test
    void listOrders_ShouldReturnOrdersList() {
        webTestClient.get()
                .uri("/admin/orders")
                .exchange()
                .expectStatus().isOk();

        verify(orderService).getAllOrders(anyLong());
    }
} 