package com.ecommerce.store.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request payload for updating an existing product. All fields are optional - only provided fields will be updated.")
public record UpdateProductRequest(
        @Schema(description = "Product name", example = "iPhone 15 Pro Max")
        @Size(max = 256, message = "Product name must not exceed 256 characters")
        String name,

        @Schema(description = "Product description", example = "The ultimate flagship smartphone from Apple")
        @Size(max = 1024, message = "Product description must not exceed 1024 characters")
        String description,

        @Schema(description = "Product price", example = "1199.99")
        @Positive(message = "Product price must be positive")
        BigDecimal price,

        @Schema(description = "Stock Keeping Unit (SKU)", example = "IPHONE-15-PROMAX-512")
        @Size(max = 64, message = "SKU must not exceed 64 characters")
        String sku,

        @Schema(description = "Whether the product is active", example = "true")
        Boolean active
) {
}
