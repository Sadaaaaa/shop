package com.example.shop.service;

import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Override
    public Mono<Cart> getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                }))
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(item -> productService.findProductById(item.getProductId())
                                .map(product -> {
                                    item.setProduct(product);
                                    return item;
                                }))
                        .collectList()
                        .map(items -> {
                            cart.setItems(items);
                            return cart;
                        }));
    }

    @Override
    public Mono<Cart> addItemToCart(Long userId, CartItem item) {
        return getCart(userId)
                .flatMap(cart -> {
                    item.setCartId(cart.getId());
                    return cartItemRepository.save(item)
                            .then(getCart(userId));
                });
    }

    @Override
    public Mono<Cart> removeItemFromCart(Long userId, Long productId) {
        return getCart(userId)
                .flatMap(cart -> cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId)
                        .then(getCart(userId)));
    }

    @Override
    public Mono<Cart> updateItemQuantity(Long userId, Long productId, int quantity) {
        return getCart(userId)
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .filter(item -> item.getProductId().equals(productId))
                        .next()
                        .flatMap(item -> {
                            item.setQuantity(quantity);
                            return cartItemRepository.save(item);
                        })
                        .then(getCart(userId)));
    }

    @Override
    public Mono<Void> clearCart(Long userId) {
        return getCart(userId)
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(cartItemRepository::delete)
                        .then());
    }

    @Override
    public Mono<Integer> getCartCounter(Long userId) {
        return getCart(userId)
                .map(cart -> cart.getItems().size());
    }

    @Override
    public Mono<Integer> getProductsCounter(Long userId, Long productId) {
        return getCart(userId)
                .map(cart -> cart.getItems().stream()
                        .filter(item -> item.getProductId().equals(productId))
                        .mapToInt(CartItem::getQuantity)
                        .sum());
    }
}
