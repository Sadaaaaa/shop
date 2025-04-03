package com.example.shop.controller;

import com.example.shop.BaseTest;
import com.example.shop.TestData;
import com.example.shop.model.Cart;
import com.example.shop.model.Product;
import com.example.shop.service.CartService;
import com.example.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ProductControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

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
        
        Page<Product> productPage = new PageImpl<>(testProducts);
        when(productService.findAllProducts(any(Pageable.class))).thenReturn(productPage);
        when(productService.findProductById(testProduct.getId())).thenReturn(testProduct);
        when(cartService.getCart(any())).thenReturn(testCart);
    }

    @Test
    void showProducts_ShouldReturnProductsPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    void viewProduct_ExistingProduct_ShouldReturnProductPage() throws Exception {
        mockMvc.perform(get("/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void showProducts_WithSearchQuery_ShouldReturnFilteredProducts() throws Exception {
        String keyword = "test";
        when(productService.findProductsByNameOrDescription(eq(keyword), any(Pageable.class)))
            .thenReturn(new PageImpl<>(testProducts.subList(0, 1)));

        mockMvc.perform(get("/")
                .param("search", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    void getProductImage_ExistingImage_ShouldReturnImage() throws Exception {
        byte[] imageData = "test image content".getBytes();
        testProduct.setImage(imageData);
        when(productService.findProductById(testProduct.getId())).thenReturn(testProduct);

        mockMvc.perform(get("/products/image/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }
} 