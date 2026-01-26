package com.ecommerce.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EcommerceStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceStoreApplication.class, args);
    }
}
