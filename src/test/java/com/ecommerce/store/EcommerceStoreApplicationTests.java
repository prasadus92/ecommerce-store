package com.ecommerce.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context Tests")
class EcommerceStoreApplicationTests {

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        // Context loading test - verifies Spring configuration is valid
    }
}
