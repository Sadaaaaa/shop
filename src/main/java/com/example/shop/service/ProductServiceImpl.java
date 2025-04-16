package com.example.shop.service;

import com.example.shop.model.Product;
import com.example.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final DatabaseClient databaseClient;

    @Override
    public Flux<Product> getAllProducts() {
        return productRepository.findAllProducts();
    }

    @Override
    public Mono<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Flux<Product> searchProducts(String name, String description) {
        if (name != null && description != null) {
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(name, description);
        } else if (name != null) {
            return productRepository.findByNameContainingIgnoreCase(name);
        }
        return Flux.empty();
    }

    @Override
    public Flux<Product> filterProductsByPrice(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    public Flux<Product> findWithFilters(String name, Double minPrice, Double maxPrice) {
        return productRepository.findWithFilters(name, minPrice, maxPrice);
    }

    @Override
    public Mono<Product> saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Mono<Void> deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }

    @Override
    public Mono<Page<Product>> findProductsByNameOrDescription(String search, int page, int size, String sort) {
        final Sort.Direction direction;
        final String property;

        if (sort != null) {
            switch (sort) {
                case "name_desc" -> {
                    direction = Sort.Direction.DESC;
                    property = "name";
                }
                case "price_asc" -> {
                    direction = Sort.Direction.ASC;
                    property = "price";
                }
                case "price_desc" -> {
                    direction = Sort.Direction.DESC;
                    property = "price";
                }
                default -> {
                    direction = Sort.Direction.ASC;
                    property = "name";
                }
            }
        } else {
            direction = Sort.Direction.ASC;
            property = "name";
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, property));

        Flux<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search)
                    .sort((p1, p2) -> {
                        if (property.equals("name")) {
                            return direction == Sort.Direction.ASC ?
                                    p1.getName().compareToIgnoreCase(p2.getName()) :
                                    p2.getName().compareToIgnoreCase(p1.getName());
                        } else {
                            return direction == Sort.Direction.ASC ?
                                    Double.compare(p1.getPrice(), p2.getPrice()) :
                                    Double.compare(p2.getPrice(), p1.getPrice());
                        }
                    });
        } else {
            products = productRepository.findAllProducts()
                    .sort((p1, p2) -> {
                        if (property.equals("name")) {
                            return direction == Sort.Direction.ASC ?
                                    p1.getName().compareToIgnoreCase(p2.getName()) :
                                    p2.getName().compareToIgnoreCase(p1.getName());
                        } else {
                            return direction == Sort.Direction.ASC ?
                                    Double.compare(p1.getPrice(), p2.getPrice()) :
                                    Double.compare(p2.getPrice(), p1.getPrice());
                        }
                    });
        }

        return products
                .skip(page * (long) size)
                .take(size)
                .collectList()
                .zipWith(products.count())
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }

    @Override
    public Mono<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Mono<ByteBuffer> findProductImageById(Long id) {
        return databaseClient.sql("SELECT image FROM products WHERE id = :id")
                .bind("id", id)
                .map((row, metadata) -> {
                    Object img = row.get("image");
                    if (img instanceof ByteBuffer buffer) {
                        ByteBuffer duplicate = buffer.duplicate();
                        duplicate.rewind();
                        return duplicate;
                    }
                    return null;
                })
                .first();
    }
}
