package com.example.shop.controller;

import com.example.shop.BaseTest;
import com.example.shop.TestData;
import com.example.shop.model.Cart;
import com.example.shop.model.Product;
import com.example.shop.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@AutoConfigureMockMvc
class CartControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        testCart = TestData.createTestCart();
        testCart.getItems().add(TestData.createTestCartItem(testProduct));

        when(cartService.getCart(any())).thenReturn(testCart);
        when(cartService.addToCart(any(), any())).thenReturn(testCart);
        when(cartService.decreaseItems(any(), any())).thenReturn(testCart);
        when(cartService.getCartCounter(any())).thenReturn(testCart.getItems().size());
    }

    @Test
    void addToCart_ValidProduct_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .param("productId", testProduct.getId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(cartService).addToCart(any(), eq(testProduct.getId()));
    }

    @Test
    void decreaseItems_ValidProduct_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/cart/decrease")
                        .param("productId", testProduct.getId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(cartService).decreaseItems(any(), eq(testProduct.getId()));
    }

    @Test
    void removeFromCart_ValidProduct_ShouldReturnOk() throws Exception {
        doNothing().when(cartService).removeFromCart(any(), any());

        mockMvc.perform(post("/cart/remove")
                        .param("productId", testProduct.getId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(cartService).removeFromCart(any(), eq(testProduct.getId()));
    }

    @Test
    void getCart_ShouldReturnCartPage() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("cart"));

        verify(cartService).getCart(any());
    }

    @Test
    void getCartCounter_ShouldReturnItemCount() throws Exception {
        mockMvc.perform(get("/cart/counter"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }
} 