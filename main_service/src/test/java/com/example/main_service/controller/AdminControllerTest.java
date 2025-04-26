package com.example.main_service.controller;

import com.example.main_service.model.Product;
import com.example.main_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private Model model;

    @Mock
    private FilePart filePart;

    @InjectMocks
    private AdminController adminController;

    private Product testProduct;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(100.0)
                .build();

        testProducts = List.of(testProduct);
    }

    @Test
    void adminPanel_ShouldReturnAdminPanelPage() {
        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(testProducts));
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        StepVerifier.create(adminController.adminPanel(model))
                .expectNext("admin/panel")
                .verifyComplete();

        verify(model).addAttribute("products", testProducts);
        verify(model).addAttribute("newProduct", new Product());
    }

    @Test
    void addProduct_ShouldAddProductWithoutImage() {
        when(productService.saveProduct(any(Product.class))).thenReturn(Mono.just(testProduct));

        StepVerifier.create(adminController.addProduct("Test Product", "Test Description", "100.0", null, model))
                .expectNext("redirect:/admin")
                .verifyComplete();

        verify(productService).saveProduct(any(Product.class));
    }

    @Test
    void addProduct_ShouldAddProductWithImage() {
        byte[] imageBytes = "test image".getBytes();
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(imageBytes);

        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(filePart.filename()).thenReturn("test.jpg");
        when(productService.saveProduct(any(Product.class))).thenReturn(Mono.just(testProduct));

        StepVerifier.create(adminController.addProduct("Test Product", "Test Description", "100.0", filePart, model))
                .expectNext("redirect:/admin")
                .verifyComplete();

        verify(productService).saveProduct(any(Product.class));
    }

    @Test
    void addProduct_ShouldHandleInvalidPrice() {
        StepVerifier.create(adminController.addProduct("Test Product", "Test Description", "invalid", null, model))
                .expectNext("admin/panel")
                .verifyComplete();

        verify(model).addAttribute(eq("error"), anyString());
        verify(productService, never()).saveProduct(any(Product.class));
    }

    @Test
    void addProduct_ShouldHandleSaveError() {
        when(productService.saveProduct(any(Product.class)))
                .thenReturn(Mono.error(new RuntimeException("Save error")));

        StepVerifier.create(adminController.addProduct("Test Product", "Test Description", "100.0", null, model))
                .expectNext("admin/panel")
                .verifyComplete();

        verify(model).addAttribute(eq("error"), anyString());
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() {
        when(productService.deleteProduct(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(adminController.deleteProduct(1L, model))
                .expectNext("redirect:/admin")
                .verifyComplete();

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProduct_ShouldHandleDataIntegrityViolation() {
        when(productService.deleteProduct(anyLong()))
                .thenReturn(Mono.error(new DataIntegrityViolationException("Cannot delete")));
        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(testProducts));
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        StepVerifier.create(adminController.deleteProduct(1L, model))
                .expectNext("admin/panel")
                .verifyComplete();

        verify(model).addAttribute(eq("error"), anyString());
        verify(model).addAttribute("products", testProducts);
    }

    @Test
    void deleteProduct_ShouldHandleGeneralError() {
        when(productService.deleteProduct(anyLong()))
                .thenReturn(Mono.error(new RuntimeException("Delete error")));
        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(testProducts));
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        StepVerifier.create(adminController.deleteProduct(1L, model))
                .expectNext("admin/panel")
                .verifyComplete();

        verify(model).addAttribute(eq("error"), anyString());
        verify(model).addAttribute("products", testProducts);
    }
} 