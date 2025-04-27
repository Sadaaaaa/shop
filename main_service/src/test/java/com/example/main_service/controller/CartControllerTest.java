package com.example.main_service.controller;

import com.example.main_service.dto.UpdateQuantityRequest;
import com.example.main_service.model.Cart;
import com.example.main_service.model.CartItem;
import com.example.main_service.model.Product;
import com.example.main_service.service.CartService;
import com.example.main_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @Mock
    private Model model;

    @InjectMocks
    private CartController cartController;

    private Cart testCart;
    private CartItem testCartItem;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(100.0);

        testCartItem = new CartItem();
        testCartItem.setProductId(1L);
        testCartItem.setQuantity(1);
        testCartItem.setPrice(100.0);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(1L);
        testCart.setItems(new ArrayList<>(List.of(testCartItem)));
    }

    @Test
    void getCartPage_ShouldReturnCartPage() {
        when(cartService.getCart(anyLong())).thenReturn(Mono.just(testCart));
        when(model.addAttribute(anyString(), any())).thenReturn(model);

        StepVerifier.create(cartController.getCartPage(model))
                .expectNext("cart")
                .verifyComplete();

        verify(model).addAttribute("cart", testCart);
    }

    @Test
    void getCart_ShouldReturnCart() {
        when(cartService.getCart(anyLong())).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartController.getCart())
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void getCartCounter_ShouldReturnCounter() {
        when(cartService.getCartCounter(anyLong())).thenReturn(Mono.just(1));

        StepVerifier.create(cartController.getCartCounter())
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void getProductsCounter_ShouldReturnProductCount() {
        when(cartService.getProductsCounter(anyLong(), anyLong())).thenReturn(Mono.just(1));

        StepVerifier.create(cartController.getProductsCounter(1L))
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void getProductsCounter_ShouldThrowException_WhenProductIdIsNull() {
        StepVerifier.create(cartController.getProductsCounter(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void addToCart_ShouldAddNewItem() {
        when(cartService.getCart(anyLong())).thenReturn(Mono.just(new Cart()));
        when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));
        when(cartService.addItemToCart(anyLong(), any())).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartController.addToCart(1L))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void addToCart_ShouldUpdateExistingItem() {
        when(cartService.getCart(anyLong())).thenReturn(Mono.just(testCart));
        when(cartService.updateItemQuantity(anyLong(), anyLong(), anyInt())).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartController.addToCart(1L))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void decreaseItems_ShouldDecreaseQuantity() {
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setPrice(100.0);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartService.getCart(anyLong())).thenReturn(Mono.just(cart));
        when(cartService.updateItemQuantity(anyLong(), anyLong(), eq(1))).thenReturn(Mono.just(cart));

        StepVerifier.create(cartController.decreaseItems(1L))
                .expectNext(cart)
                .verifyComplete();

        verify(cartService).getCart(anyLong());
        verify(cartService).updateItemQuantity(anyLong(), eq(1L), eq(1));
    }

    @Test
    void decreaseItems_ShouldRemoveItem_WhenQuantityIsOne() {
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setQuantity(1);
        item.setPrice(100.0);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setItems(new ArrayList<>(List.of(item)));

        Cart emptyCart = new Cart();
        emptyCart.setId(1L);
        emptyCart.setUserId(1L);
        emptyCart.setItems(new ArrayList<>());

        when(cartService.getCart(anyLong())).thenReturn(Mono.just(cart));
        when(cartService.removeItemFromCart(anyLong(), anyLong())).thenReturn(Mono.just(emptyCart));

        StepVerifier.create(cartController.decreaseItems(1L))
                .expectNext(emptyCart)
                .verifyComplete();

        verify(cartService).getCart(anyLong());
        verify(cartService).removeItemFromCart(anyLong(), eq(1L));
    }

    @Test
    void decreaseItems_ShouldThrowException_WhenProductIdIsNull() {
        StepVerifier.create(cartController.decreaseItems(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void decreaseItems_ShouldThrowException_WhenItemNotFound() {
        Cart emptyCart = new Cart();
        emptyCart.setId(1L);
        emptyCart.setUserId(1L);
        emptyCart.setItems(new ArrayList<>());

        when(cartService.getCart(anyLong())).thenReturn(Mono.just(emptyCart));

        StepVerifier.create(cartController.decreaseItems(1L))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void removeFromCart_ShouldRemoveItem() {
        when(cartService.removeItemFromCart(anyLong(), anyLong())).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartController.removeFromCart(1L))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void updateQuantity_ShouldUpdateItemQuantity() {
        UpdateQuantityRequest request = new UpdateQuantityRequest(1L, 2);
        when(cartService.updateItemQuantity(anyLong(), anyLong(), anyInt())).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartController.updateQuantity(request))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void addItemToCart_ShouldAddItem() {
        when(cartService.addItemToCart(anyLong(), any())).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartController.addItemToCart(testCartItem))
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCodeValue());
                    assertEquals(testCart, response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void removeItemFromCart_ShouldRemoveItemAndReturnResponse() {
        when(cartService.removeItemFromCart(anyLong(), anyLong())).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartController.removeItemFromCart(1L))
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCodeValue());
                    assertEquals(testCart, response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void updateItemQuantity_ShouldUpdateQuantityAndReturnResponse() {
        when(cartService.updateItemQuantity(anyLong(), anyLong(), anyInt())).thenReturn(Mono.just(testCart));

        StepVerifier.create(cartController.updateItemQuantity(1L, 2))
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCodeValue());
                    assertEquals(testCart, response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void clearCart_ShouldClearCartAndReturnResponse() {
        when(cartService.clearCart(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(cartController.clearCart())
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCodeValue());
                    assertNull(response.getBody());
                })
                .verifyComplete();
    }
} 