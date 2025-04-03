package com.example.shop.service;

import com.example.shop.BaseTest;
import com.example.shop.TestData;
import com.example.shop.model.Product;
import com.example.shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest extends BaseTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductRepository productRepository;

    private Product testProduct;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProduct = TestData.createTestProduct();
        testProducts = TestData.createTestProducts(3);

        when(productRepository.findById(testProduct.getId()))
                .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findAll())
                .thenReturn(testProducts);
        when(productRepository.findAllProducts(any(Pageable.class)))
                .thenReturn(new PageImpl<>(testProducts));
    }

    @Test
    void findProductById_ExistingProduct_ShouldReturnProduct() {
        Product result = productService.findProductById(testProduct.getId());

        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
    }

    @Test
    void findAllProducts_ShouldReturnPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productService.findAllProducts(pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
    }

    @Test
    void saveProduct_ValidProduct_ShouldSaveAndReturnProduct() {
        Product newProduct = new Product();
        newProduct.setName("Новый продукт");
        newProduct.setDescription("Описание нового продукта");
        newProduct.setPrice(199.99);
        Product result = productService.saveProduct(newProduct);

        assertNotNull(result);
        assertEquals(newProduct.getName(), result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_ExistingProduct_ShouldDelete() {
        productService.deleteProduct(testProduct.getId());

        verify(productRepository).deleteById(testProduct.getId());
    }

    @Test
    void findProductsByName_WithKeyword_ShouldReturnMatchingProducts() {
        String keyword = "тест";
        when(productRepository.findByNameContainingIgnoreCase(eq(keyword), any(Pageable.class)))
                .thenReturn(new PageImpl<>(testProducts.subList(0, 1)));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productService.findProductsByName(keyword, pageable);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() {
        List<Product> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(productRepository).findAll();
    }
} 