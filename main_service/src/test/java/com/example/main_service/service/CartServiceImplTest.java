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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private static final Long CART_ID = 1L;
    private static final Long PRODUCT_ID = 1L;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .price(100.0)
                .build();

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCartId(CART_ID);
        testCartItem.setProductId(PRODUCT_ID);
        testCartItem.setQuantity(1);
        testCartItem.setPrice(100.0);
        testCartItem.setProduct(testProduct);

        testCart = new Cart();
        testCart.setId(CART_ID);
        testCart.setUserId(USER_ID);
        testCart.setItems(new ArrayList<>(List.of(testCartItem)));
    }

    @Test
    void getCart_WhenCartExists_ShouldReturnCartWithItems() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(productService.findProductById(PRODUCT_ID)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.getCart(USER_ID))
                .expectNextMatches(cart ->
                        cart.getId().equals(CART_ID) &&
                                cart.getUserId().equals(USER_ID) &&
                                cart.getItems().size() == 1 &&
                                cart.getItems().get(0).getProduct().equals(testProduct))
                .verifyComplete();
    }

    @Test
    void getCart_WhenCartDoesNotExist_ShouldCreateNewCart() {
        Cart newCart = new Cart();
        newCart.setUserId(USER_ID);
        newCart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(newCart));
        when(cartItemRepository.findByCartId(null)).thenReturn(Flux.empty());

        StepVerifier.create(cartService.getCart(USER_ID))
                .expectNextMatches(cart ->
                        cart.getUserId().equals(USER_ID) &&
                                cart.getItems().isEmpty())
                .verifyComplete();

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCartByUserId_WhenCartExists_ShouldReturnCart() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.just(testCartItem));
        when(productService.findProductById(PRODUCT_ID)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.getCart(USER_ID))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void getCartByUserId_WhenCartDoesNotExist_ShouldCreateNewCart() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.empty());
        Cart newCart = new Cart();
        newCart.setUserId(USER_ID);
        newCart.setItems(new ArrayList<>());
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(newCart));
        when(cartItemRepository.findByCartId(any())).thenReturn(Flux.empty());

        StepVerifier.create(cartService.getCart(USER_ID))
                .expectNext(newCart)
                .verifyComplete();
    }

    @Test
    void addItemToCart_WhenCartExists_ShouldAddItem() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(productService.findProductById(PRODUCT_ID)).thenReturn(Mono.just(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(testCartItem));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.fromIterable(testCart.getItems()));

        StepVerifier.create(cartService.addItemToCart(USER_ID, testCartItem))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void removeItemFromCart_WhenItemExists_ShouldRemoveItem() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.deleteByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(Flux.empty());
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(productService.findProductById(PRODUCT_ID)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.removeItemFromCart(USER_ID, PRODUCT_ID))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void updateItemQuantity_WhenItemExists_ShouldUpdateQuantity() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(testCartItem));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(productService.findProductById(PRODUCT_ID)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.updateItemQuantity(USER_ID, PRODUCT_ID, 2))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void clearCart_WhenCartExists_ShouldClearItems() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(cartItemRepository.delete(any(CartItem.class))).thenReturn(Mono.empty());
        when(productService.findProductById(PRODUCT_ID)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.clearCart(USER_ID))
                .verifyComplete();
    }

    @Test
    void getCartCounter_ShouldReturnCorrectItemsCount() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(productService.findProductById(PRODUCT_ID)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.getCartCounter(USER_ID))
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void getProductsCounter_WhenProductExists_ShouldReturnQuantity() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(productService.findProductById(PRODUCT_ID)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.getProductsCounter(USER_ID, PRODUCT_ID))
                .expectNext(1)
                .verifyComplete();
    }

    @Test
    void getProductsCounter_WhenProductDoesNotExist_ShouldReturnZero() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.fromIterable(testCart.getItems()));
        when(productService.findProductById(PRODUCT_ID)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.getProductsCounter(USER_ID, 999L))
                .expectNext(0)
                .verifyComplete();
    }
}