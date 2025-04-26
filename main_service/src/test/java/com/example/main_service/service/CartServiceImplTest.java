package com.example.main_service.service;

import com.example.main_service.model.Cart;
import com.example.main_service.model.CartItem;
import com.example.main_service.model.Product;
import com.example.main_service.repository.CartItemRepository;
import com.example.main_service.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private CartItem testCartItem;
    private Product testProduct;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(100.0)
                .build();

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCartId(1L);
        testCartItem.setProductId(1L);
        testCartItem.setQuantity(1);
        testCartItem.setPrice(100.0);
        testCartItem.setProduct(testProduct);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(USER_ID);
        testCart.setItems(new ArrayList<>(List.of(testCartItem)));
    }

    @Test
    void getCart_ShouldReturnExistingCart() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.getCart(USER_ID))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void getCart_ShouldCreateNewCart_WhenNotExists() {
        Cart newCart = new Cart();
        newCart.setUserId(USER_ID);
        newCart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(newCart));
        when(cartItemRepository.findByCartId(null)).thenReturn(Flux.empty());

        StepVerifier.create(cartService.getCart(USER_ID))
                .expectNext(newCart)
                .verifyComplete();
    }

    @Test
    void addItemToCart_ShouldAddNewItem() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(testCartItem));
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.addItemToCart(USER_ID, testCartItem))
                .expectNext(testCart)
                .verifyComplete();

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void removeItemFromCart_ShouldRemoveItem() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(cartItemRepository.deleteByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Flux.empty());
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.removeItemFromCart(USER_ID, 1L))
                .expectNext(testCart)
                .verifyComplete();

        verify(cartItemRepository).deleteByCartIdAndProductId(anyLong(), anyLong());
    }

    @Test
    void updateItemQuantity_ShouldUpdateQuantity() {
        CartItem updatedItem = new CartItem();
        updatedItem.setId(1L);
        updatedItem.setCartId(1L);
        updatedItem.setProductId(1L);
        updatedItem.setQuantity(2);
        updatedItem.setPrice(100.0);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Flux.just(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(updatedItem));
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.updateItemQuantity(USER_ID, 1L, 2))
                .expectNext(testCart)
                .verifyComplete();

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void clearCart_ShouldRemoveAllItems() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(cartItemRepository.delete(any(CartItem.class))).thenReturn(Mono.empty());
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.clearCart(USER_ID))
                .verifyComplete();

        verify(cartItemRepository).delete(any(CartItem.class));
    }

    @Test
    void getCartCounter_ShouldReturnItemsCount() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.getCartCounter(USER_ID))
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void getProductsCounter_ShouldReturnProductQuantity() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.getProductsCounter(USER_ID, 1L))
                .expectNext(1)
                .verifyComplete();
    }
}