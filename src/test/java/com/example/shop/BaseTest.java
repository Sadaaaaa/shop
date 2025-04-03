package com.example.shop;

import com.example.shop.config.TestConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
public abstract class BaseTest {
    protected static final Long TEST_USER_ID = 1L;
    protected static final Long TEST_PRODUCT_ID = 1L;
    
    protected void clearDatabase() {
    }
} 