package com.example.shop.controller;

import com.example.shop.BaseTest;
import com.example.shop.TestData;
import com.example.shop.model.Product;
import com.example.shop.model.Order;
import com.example.shop.service.ProductService;
import com.example.shop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AdminControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

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
    }

    @Test
    void adminPanel_ShouldReturnAdminPage() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/panel"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("newProduct"));
    }

    @Test
    void addProduct_ValidProduct_ShouldRedirectToAdmin() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        mockMvc.perform(multipart("/admin/products/add")
                .file(imageFile)
                .param("name", "Test Product")
                .param("description", "Test Description")
                .param("price", "99.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productService).saveProduct(any(Product.class));
    }

    @Test
    void deleteProduct_ExistingProduct_ShouldRedirectToAdmin() throws Exception {
        mockMvc.perform(post("/admin/products/delete/{id}", testProduct.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productService).deleteProduct(testProduct.getId());
    }

    @Test
    void listProducts_ShouldReturnProductsList() throws Exception {
        List<Product> products = TestData.createTestProducts(3);
        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(products));

        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", products));
    }

    @Test
    void listOrders_ShouldReturnOrdersList() throws Exception {
        List<Order> testOrders = List.of(TestData.createTestOrder());
        when(orderService.getAllOrders(1L)).thenReturn(Flux.fromIterable(testOrders));

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("orders", testOrders));
    }
} 