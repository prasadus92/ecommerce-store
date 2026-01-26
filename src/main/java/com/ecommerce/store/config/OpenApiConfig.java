package com.ecommerce.store.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:E-commerce Store API}")
    private String applicationName;

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName)
                        .description("""
                                Production-grade REST API for E-commerce Store management.

                                ## Features
                                - **Product Management**: Create, update, and retrieve products with pricing
                                - **Order Management**: Place orders with multiple items and retrieve orders by date range
                                - **Price Integrity**: Product price changes don't affect historical order totals

                                ## API Versioning
                                This API uses URL path versioning (v1). Future versions will be available at `/api/v2/...`
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("E-commerce Team")
                                .email("api-support@ecommerce.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("/").description("Current Server")));
    }
}
