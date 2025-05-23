package com.example.main_service.repository;

import com.example.main_service.model.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductRepository extends R2dbcRepository<Product, Long> {

    @Query("SELECT * FROM products WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Flux<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT * FROM products WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :description, '%'))")
    Flux<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("name") String name,
            @Param("description") String description);

    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice")
    Flux<Product> findByPriceBetween(
            @Param("minPrice") double minPrice,
            @Param("maxPrice") double maxPrice);

    @Query("SELECT * FROM products WHERE (:name IS NULL OR LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:minPrice IS NULL OR price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR price <= :maxPrice)")
    Flux<Product> findWithFilters(
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);

    @Query("SELECT * FROM products")
    Flux<Product> findAllProducts();
}