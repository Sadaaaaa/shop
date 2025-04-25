package com.example.main_service.service;

import com.example.main_service.model.PageWrapper;
import com.example.main_service.model.Product;
import com.example.main_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final DatabaseClient databaseClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String PRODUCTS_LIST_KEY = "products:list";
    private static final long CACHE_TTL = 10;

    @Override
    public Flux<Product> getAllProducts() {
        @SuppressWarnings("unchecked")
        List<Product> cachedProducts = (List<Product>) redisTemplate.opsForValue().get(PRODUCTS_LIST_KEY);
        if (cachedProducts != null) {
            log.info("Получен список товаров из кэша Redis");
            return Flux.fromIterable(cachedProducts);
        }
        
        log.info("Список товаров не найден в кэше Redis, загружаем из БД");
        return productRepository.findAllProducts()
                .collectList()
                .flatMapMany(products -> {
                    redisTemplate.opsForValue().set(PRODUCTS_LIST_KEY, products, CACHE_TTL, TimeUnit.MINUTES);
                    return Flux.fromIterable(products);
                });
    }

    @Override
    public Mono<Product> saveProduct(Product product) {
        log.info("Сохранение товара: {}", product);
        return productRepository.save(product)
                .doOnNext(savedProduct -> {
                    String key = PRODUCT_KEY_PREFIX + savedProduct.getId();
                    redisTemplate.opsForValue().set(key, savedProduct, CACHE_TTL, TimeUnit.MINUTES);
                    redisTemplate.delete(PRODUCTS_LIST_KEY);
                    log.info("Товар с ID {} сохранен в кэш Redis, список товаров очищен", savedProduct.getId());
                });
    }

    @Override
    public Mono<Void> deleteProduct(Long id) {
        return productRepository.deleteById(id)
                .doOnSuccess(v -> {
                    redisTemplate.delete(PRODUCT_KEY_PREFIX + id);
                    redisTemplate.delete(PRODUCTS_LIST_KEY);
                    log.info("Товар с ID {} удален из кэша Redis, список товаров очищен", id);
                });
    }

    @Override
    public Mono<Page<Product>> findProductsByNameOrDescription(String search, int page, int size, String sort) {
        final Sort.Direction direction;
        final String property;

        if (sort != null) {
            switch (sort) {
                case "name_desc":
                    direction = Sort.Direction.DESC;
                    property = "name";
                    break;
                case "price_asc":
                    direction = Sort.Direction.ASC;
                    property = "price";
                    break;
                case "price_desc":
                    direction = Sort.Direction.DESC;
                    property = "price";
                    break;
                default:
                    direction = Sort.Direction.ASC;
                    property = "name";
            }
        } else {
            direction = Sort.Direction.ASC;
            property = "name";
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, property));
        String cacheKey = String.format("products:search:%s:page%d:size%d:sort%s", 
            search != null ? search : "all", page, size, sort != null ? sort : "default");

        @SuppressWarnings("unchecked")
        PageWrapper<Product> cachedPage = (PageWrapper<Product>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPage != null) {
            log.info("Результаты поиска получены из кэша Redis");
            return Mono.just(cachedPage.toPage());
        }

        log.info("Результаты поиска не найдены в кэше Redis, загружаем из БД");
        Flux<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        } else {
            products = productRepository.findAllProducts();
        }

        return products
                .collectList()
                .map(allProducts -> {
                    int start = (int) pageRequest.getOffset();
                    int end = Math.min((start + pageRequest.getPageSize()), allProducts.size());
                    List<Product> pageContent = allProducts.subList(start, end);
                    PageImpl<Product> page1 = new PageImpl<>(pageContent, pageRequest, allProducts.size());
                    PageWrapper<Product> wrapper = PageWrapper.fromPage(page1);
                    redisTemplate.opsForValue().set(cacheKey, wrapper, CACHE_TTL, TimeUnit.MINUTES);
                    return page1;
                });
    }

    @Override
    public Mono<Product> findProductById(Long id) {
        String key = PRODUCT_KEY_PREFIX + id;
        Product cachedProduct = (Product) redisTemplate.opsForValue().get(key);
        if (cachedProduct != null) {
            log.info("Товар с ID {} получен из кэша Redis", id);
            return Mono.just(cachedProduct);
        }
        
        log.info("Товар с ID {} не найден в кэше Redis, загружаем из БД", id);
        return productRepository.findById(id)
                .doOnNext(product -> {
                    redisTemplate.opsForValue().set(key, product, CACHE_TTL, TimeUnit.MINUTES);
                });
    }

    @Override
    public Mono<ByteBuffer> findProductImageById(Long id) {
        return databaseClient.sql("SELECT image FROM products WHERE id = :id")
                .bind("id", id)
                .map((row, metadata) -> {
                    Object img = row.get("image");
                    if (img instanceof ByteBuffer) {
                        ByteBuffer buffer = (ByteBuffer) img;
                        ByteBuffer duplicate = buffer.duplicate();
                        duplicate.rewind();
                        return duplicate;
                    }
                    return null;
                })
                .first();
    }
}
