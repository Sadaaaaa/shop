package com.example.shop.service;

import com.example.shop.TestData;
import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import com.example.shop.model.Product;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

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
        testCart.getItems().add(testCartItem);

        lenient().when(cartRepository.findByUserId(anyLong())).thenReturn(Mono.just(testCart));
        lenient().when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(testCart));
        lenient().when(cartItemRepository.findByCartId(anyLong())).thenReturn(Flux.just(testCartItem));
        lenient().when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(testCartItem));
        lenient().when(cartItemRepository.deleteByCartIdAndProductId(anyLong(), anyLong())).thenReturn(Flux.empty());
        lenient().when(productRepository.findById(anyLong())).thenReturn(Mono.just(testProduct));
        lenient().when(productService.findProductById(anyLong())).thenReturn(Mono.just(testProduct));
    }

    @Test
    void getCart_ShouldReturnCart() {
        StepVerifier.create(cartService.getCart(1L))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void addItemToCart_ShouldAddItemAndReturnCart() {
        StepVerifier.create(cartService.addItemToCart(1L, testCartItem))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void removeItemFromCart_ShouldRemoveItemAndReturnCart() {
        StepVerifier.create(cartService.removeItemFromCart(1L, 1L))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void updateItemQuantity_ShouldUpdateQuantityAndReturnCart() {
        StepVerifier.create(cartService.updateItemQuantity(1L, 1L, 5))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void getCartCounter_ShouldReturnItemCount() {
        StepVerifier.create(cartService.getCartCounter(1L))
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void getProductsCounter_ShouldReturnProductCount() {
        StepVerifier.create(cartService.getProductsCounter(1L, 1L))
                .expectNext(1)
                .verifyComplete();
    }
} 