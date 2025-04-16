package com.example.shop.controller;

import com.example.shop.TestData;
import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private Product testProduct;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        testProducts = TestData.createTestProducts(3);

        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(testProducts));
        when(productService.saveProduct(any(Product.class))).thenReturn(Mono.just(testProduct));
        when(productService.deleteProduct(anyLong())).thenReturn(Mono.empty());
    }

    @Test
    void adminPanel_ShouldReturnAdminPage() {
        webTestClient.get()
                .uri("/admin")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML_VALUE);
    }

    @Test
    void addProduct_ValidProduct_ShouldRedirectToAdmin() {
        Product expectedProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .build();
        when(productService.saveProduct(any(Product.class))).thenReturn(Mono.just(expectedProduct));

        webTestClient.post()
                .uri("/admin/products/add")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("name", "Test Product")
                        .with("description", "Test Description")
                        .with("price", "99.99"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/admin");

        verify(productService).saveProduct(argThat(product ->
                product.getName().equals("Test Product") &&
                        product.getDescription().equals("Test Description") &&
                        product.getPrice() == 99.99
        ));
    }

    @Test
    void deleteProduct_ExistingProduct_ShouldRedirectToAdmin() {
        when(productService.deleteProduct(1L)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/admin/products/delete/1")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/admin");

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProduct_ProductInOrder_ShouldReturnError() {
        when(productService.deleteProduct(1L))
                .thenReturn(Mono.error(new DataIntegrityViolationException(
                        "update or delete on table \"products\" violates foreign key constraint \"order_items_product_id_fkey\" on table \"order_items\"")));
        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(testProducts));

        webTestClient.post()
                .uri("/admin/products/delete/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML_VALUE)
                .expectBody(String.class)
                .value(body -> body.contains("Невозможно удалить товар, так как он используется в заказах"));

        verify(productService).deleteProduct(1L);
        verify(productService).getAllProducts();
    }
} 