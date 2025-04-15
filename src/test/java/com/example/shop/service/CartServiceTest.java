package com.example.shop.service;

import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import com.example.shop.model.Product;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testCart = TestData.createTestCart();
        testProduct = TestData.createTestProduct();
        testCartItem = TestData.createTestCartItem(testProduct);
    }

    @Test
    void getCart_ShouldReturnCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.getCart(1L))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void addItemToCart_ShouldAddItemAndReturnCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(testCart));
        when(productRepository.findById(1L)).thenReturn(Mono.just(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.addItemToCart(1L, testCartItem))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void removeItemFromCart_ShouldRemoveItemAndReturnCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.removeItemFromCart(1L, 1L))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void updateItemQuantity_ShouldUpdateQuantityAndReturnCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.updateItemQuantity(1L, 1L, 5))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void getCartCounter_ShouldReturnItemCount() {
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(testCart));

        Integer count = cartService.getCartCounter(1L);
        assertEquals(1, count);
    }

    @Test
    void getProductsCounter_ShouldReturnProductCount() {
        when(cartRepository.findByUserId(1L)).thenReturn(Mono.just(testCart));

        Integer count = cartService.getProductsCounter(1L, 1L);
        assertEquals(1, count);
    }
} 