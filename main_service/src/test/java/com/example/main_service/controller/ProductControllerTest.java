package com.example.main_service.controller;

import com.example.main_service.model.Cart;
import com.example.main_service.model.Product;
import com.example.main_service.service.CartService;
import com.example.main_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;
    private Cart testCart;
    private PageImpl<Product> testPage;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(100.0);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(1L);

        testPage = new PageImpl<>(List.of(testProduct));
    }

    @Test
    void showProducts_ShouldReturnProductsPage() {
        // Arrange
        when(productService.findProductsByNameOrDescription(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(Mono.just(testPage));
        when(cartService.getCart(anyLong())).thenReturn(Mono.just(testCart));
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act & Assert
        StepVerifier.create(productController.showProducts(0, 10, "name", "test", model))
                .expectNext("products")
                .verifyComplete();

        verify(model).addAttribute("products", testPage.getContent());
        verify(model).addAttribute("totalPages", testPage.getTotalPages());
        verify(model).addAttribute("currentPage", 0);
        verify(model).addAttribute("cart", testCart);
        verify(model).addAttribute(eq("param"), any(Map.class));
    }

    @Test
    void viewProduct_ShouldReturnProductPage() {
        // Arrange
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));
        when(cartService.getCart(anyLong())).thenReturn(Mono.just(testCart));
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        // Act & Assert
        StepVerifier.create(productController.viewProduct(1L, model))
                .expectNext("item")
                .verifyComplete();

        verify(model).addAttribute("product", testProduct);
        verify(model).addAttribute("cart", testCart);
    }

    @Test
    void getProductImage_ShouldReturnImage() {
        // Arrange
        byte[] testBytes = "test image".getBytes();
        ByteBuffer testBuffer = ByteBuffer.wrap(testBytes);
        when(productService.findProductImageById(anyLong())).thenReturn(Mono.just(testBuffer));

        // Act & Assert
        StepVerifier.create(productController.getProductImage(1L))
                .assertNext(response -> {
                    assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
                    assertArrayEquals(testBytes, response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void getProductImage_ShouldReturnNotFound_WhenImageNotExists() {
        // Arrange
        when(productService.findProductImageById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(productController.getProductImage(1L))
                .assertNext(response -> {
                    assertEquals(404, response.getStatusCodeValue());
                })
                .verifyComplete();
    }
} 