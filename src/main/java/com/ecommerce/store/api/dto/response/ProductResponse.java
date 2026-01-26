package com.ecommerce.store.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Product information response")
public record ProductResponse(
        @Schema(description = "Product unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Product name", example = "iPhone 15 Pro")
        String name,

        @Schema(description = "Product description", example = "The latest flagship smartphone from Apple")
        String description,

        @Schema(description = "Product price", example = "999.99")
        BigDecimal price,

        @Schema(description = "Stock Keeping Unit (SKU)", example = "IPHONE-15-PRO-256")
        String sku,

        @Schema(description = "Whether the product is active", example = "true")
        boolean active,

        @Schema(description = "Timestamp when the product was created")
        Instant createdAt,

        @Schema(description = "Timestamp when the product was last updated")
        Instant updatedAt
) {
}
