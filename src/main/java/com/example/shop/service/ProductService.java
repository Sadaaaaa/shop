package com.example.shop.service;

import com.example.shop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<Product> findAllProducts(Pageable pageable);

    Page<Product> findProductsByNameOrDescription(String query, Pageable pageable);

    Page<Product> findProductsByName(String name, Pageable pageable);

    Product findProductById(Long id);

    Product saveProduct(Product product);

    List<Product> getAllProducts();

    void deleteProduct(Long id);
}
