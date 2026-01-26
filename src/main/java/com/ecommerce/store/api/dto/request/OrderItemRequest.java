package com.ecommerce.store.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Schema(description = "Order item within an order request")
public record OrderItemRequest(
        @Schema(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Product ID is required")
        UUID productId,

        @Schema(description = "Quantity to order", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity
) {
}
