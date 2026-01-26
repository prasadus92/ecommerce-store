package com.ecommerce.store.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request payload for creating a new product")
public record CreateProductRequest(
        @Schema(description = "Product name", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Product name is required")
        @Size(max = 256, message = "Product name must not exceed 256 characters")
        String name,

        @Schema(description = "Product description", example = "The latest flagship smartphone from Apple")
        @Size(max = 1024, message = "Product description must not exceed 1024 characters")
        String description,

        @Schema(description = "Product price", example = "999.99", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Product price is required")
        @Positive(message = "Product price must be positive")
        BigDecimal price,

        @Schema(description = "Stock Keeping Unit (SKU)", example = "IPHONE-15-PRO-256")
        @Size(max = 64, message = "SKU must not exceed 64 characters")
        String sku
) {
}
