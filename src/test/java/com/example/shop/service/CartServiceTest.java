package com.example.shop.service;

import com.example.shop.BaseTest;
import com.example.shop.TestData;
import com.example.shop.model.Cart;
import com.example.shop.model.Product;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartServiceTest extends BaseTest {

    @Autowired
    private CartService cartService;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private ProductRepository productRepository;

    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        testCart = TestData.createTestCart();

        when(productRepository.findById(testProduct.getId()))
                .thenReturn(Optional.of(testProduct));
        when(cartRepository.findCartByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void addToCart_NewProduct_ShouldAddProductToCart() {
        cartService.addToCart(TEST_USER_ID, testProduct.getId());

        verify(cartRepository).save(any(Cart.class));
        assertEquals(1, testCart.getItems().size());
        assertEquals(testProduct.getId(), testCart.getItems().get(0).getProduct().getId());
        assertEquals(1, testCart.getItems().get(0).getQuantity());
    }

    @Test
    void addToCart_ExistingProduct_ShouldIncreaseQuantity() {
        cartService.addToCart(TEST_USER_ID, testProduct.getId());
        cartService.addToCart(TEST_USER_ID, testProduct.getId());

        verify(cartRepository, times(2)).save(any(Cart.class));
        assertEquals(1, testCart.getItems().size());
        assertEquals(2, testCart.getItems().get(0).getQuantity());
    }

    @Test
    void decreaseItems_ExistingProduct_ShouldDecreaseQuantity() {
        cartService.addToCart(TEST_USER_ID, testProduct.getId());
        cartService.addToCart(TEST_USER_ID, testProduct.getId());
        cartService.decreaseItems(TEST_USER_ID, testProduct.getId());

        assertEquals(1, testCart.getItems().get(0).getQuantity());
    }

    @Test
    void removeFromCart_ExistingProduct_ShouldRemoveProduct() {
        cartService.addToCart(TEST_USER_ID, testProduct.getId());
        cartService.removeFromCart(TEST_USER_ID, testProduct.getId());

        assertTrue(testCart.getItems().isEmpty());
    }

    @Test
    void updateQuantity_ValidQuantity_ShouldUpdateProductQuantity() {
        cartService.addToCart(TEST_USER_ID, testProduct.getId());
        cartService.updateQuantity(TEST_USER_ID, testProduct.getId(), 5);

        assertEquals(5, testCart.getItems().get(0).getQuantity());
    }

    @Test
    void getCart_ExistingCart_ShouldReturnCart() {
        Cart result = cartService.getCart(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
    }
} 