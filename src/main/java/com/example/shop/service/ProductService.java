package com.example.shop.service;

import com.example.shop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    public Page<Product> findAllProducts(Pageable pageable);

    public Page<Product> findProductsByNameOrDescription(String query, Pageable pageable);

    public Page<Product> findProductsByName(String name, Pageable pageable);

    public Page<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    public Page<Product> findProductsWithFilters(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    public Product findProductById(Long id);

    public void deleteProductById(Long id);

    public Product saveProduct(Product product);

    public List<Product> saveAllProducts(List<Product> products);

    public List<Product> getAllProducts();

    public void deleteProduct(Long id);
}
