package com.example.shop;

import com.example.shop.controller.AdminController;
import com.example.shop.controller.CartController;
import com.example.shop.controller.OrderController;
import com.example.shop.controller.ProductController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ShopApplicationTests {

    @Autowired
    private ProductController productController;

    @Autowired
    private CartController cartController;

    @Autowired
    private OrderController orderController;

    @Autowired
    private AdminController adminController;

    @Test
    void contextLoads() {
        assertNotNull(productController);
        assertNotNull(cartController);
        assertNotNull(orderController);
        assertNotNull(adminController);
    }
}
