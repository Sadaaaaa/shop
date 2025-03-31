package com.example.shop.service;

import com.example.shop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    Page<Product> findAllProducts(Pageable pageable);
    Page<Product> findProductsByNameOrDescription(String query, Pageable pageable);


    Page<Product> findProductsByName(String name, Pageable pageable);

    Page<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Product> findProductsWithFilters(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);



    Product findProductById(Long id);

    void deleteProductById(Long id);

    Product saveProduct(Product product);

    List<Product> saveAllProducts(List<Product> products);
}
