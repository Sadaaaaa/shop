package com.example.shop.controller;

import com.example.shop.TestData;
import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import com.example.shop.model.Product;
import com.example.shop.service.CartService;
import com.example.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CartController.class)
@Import({CartController.class, CartControllerTest.TestConfig.class})
class CartControllerTest {

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
    private CartService cartService;

    @MockBean
    private ProductService productService;

    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        testCart = TestData.createTestCart();
        testCartItem = TestData.createTestCartItem(testProduct);
        testCart.getItems().add(testCartItem);

        when(cartService.getCart(anyLong())).thenReturn(Mono.just(testCart));
        when(cartService.addItemToCart(anyLong(), any(CartItem.class))).thenReturn(Mono.just(testCart));
        when(cartService.updateItemQuantity(anyLong(), anyLong(), any(Integer.class))).thenReturn(Mono.just(testCart));
        when(cartService.removeItemFromCart(anyLong(), anyLong())).thenReturn(Mono.just(testCart));
        when(cartService.getCartCounter(anyLong())).thenReturn(Mono.just(1));
        when(cartService.getProductsCounter(anyLong(), anyLong())).thenReturn(Mono.just(1));
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));
    }

    @Test
    void getCartPage_ShouldReturnCartPage() {
        webTestClient.get()
                .uri("/cart")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getCart_ShouldReturnCart() {
        webTestClient.get()
                .uri("/cart/api")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cart.class)
                .isEqualTo(testCart);
    }

    @Test
    void getCartCounter_ShouldReturnItemCount() {
        webTestClient.get()
                .uri("/cart/counter")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .isEqualTo(1);
    }

    @Test
    void getProductsCounter_ShouldReturnProductCount() {
        webTestClient.get()
                .uri("/cart/count?productId=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .isEqualTo(1);
    }

    @Test
    void addToCart_ValidProduct_ShouldReturnCart() {
        webTestClient.post()
                .uri("/cart/add?productId=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cart.class)
                .isEqualTo(testCart);
    }

    @Test
    void decreaseItems_ValidProduct_ShouldReturnCart() {
        webTestClient.post()
                .uri("/cart/decrease?productId=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cart.class)
                .isEqualTo(testCart);
    }

    @Test
    void removeFromCart_ValidProduct_ShouldReturnCart() {
        webTestClient.post()
                .uri("/cart/remove?productId=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cart.class)
                .isEqualTo(testCart);
    }
} 