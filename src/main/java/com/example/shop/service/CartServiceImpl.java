package com.example.shop.service;

import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import com.example.shop.model.Product;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;


    @Autowired
    public CartServiceImpl(ProductRepository productRepository, CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    @Override
    public Cart getCart(Long userId) {
        return cartRepository.findCartByUserId(userId).orElse(Cart.getCart());
    }

    @Transactional
    @Override
    public List<CartItem> getCartItems(Long userId) {
        Optional<Cart> cart = cartRepository.findCartByUserId(userId);
        return cart.orElse(Cart.getCart()).getItems();
    }


    @Override
    public Integer getCartCounter(Long userId) {
        Integer quantity = cartRepository.findCartByUserId(userId).orElse(Cart.getCart()).getItems().size();
        log.info("Cart counter for user {} is {}", userId, quantity);
        return quantity;
    }

    @Override
    public Integer getProductsCounter(Long userId, Long productId) {
        List<CartItem> cartItems = cartRepository.findCartByUserId(userId).orElse(Cart.getCart()).getItems();

        return cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .map(CartItem::getQuantity)
                .findFirst()
                .orElse(0);
    }

    @Override
    public Map<Long, Integer> getCartItemsQuantity(Long userId) {
        List<CartItem> test = cartRepository.findCartByUserId(userId).orElse(Cart.getCart()).getItems();

        return test.stream().collect(Collectors.toMap(CartItem::getId, CartItem::getQuantity));
    }

    @Transactional
    @Override
    public Cart addToCart(Long userId, Long productId) {
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseGet(() -> Cart.builder().userId(userId).items(new ArrayList<>()).build());

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found!"));

                    CartItem newCartItem = CartItem.builder().product(product).quantity(0).build();
                    cart.getItems().add(newCartItem);
                    return newCartItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartRepository.save(cart);

        return cart;
    }

    @Transactional
    @Override
    public Cart decreaseItems(Long userId, Long productId) {
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElse(null);

        if (cart == null) {
            return null;
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (cartItem == null) {
            return cart;
        }

        if (cartItem.getQuantity() <= 1) {
            cart.getItems().remove(cartItem);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
        }

        cartRepository.save(cart);
        return cart;
    }

    @Transactional
    @Override
    public void removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findCartByUserId(userId)
                .orElse(null);

        if (cart == null) {
            return;
        }

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cartRepository.save(cart);
    }

    @Transactional
    @Override
    public Cart updateQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(userId, productId);
            return getCart(userId);
        }

        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseGet(() -> Cart.builder().userId(userId).items(new ArrayList<>()).build());

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found!"));

                    CartItem newCartItem = CartItem.builder().product(product).quantity(0).build();
                    cart.getItems().add(newCartItem);
                    return newCartItem;
                });

        cartItem.setQuantity(quantity);
        cartRepository.save(cart);

        return cart;
    }
}
