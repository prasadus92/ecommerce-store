package com.ecommerce.store.api.dto.response;

import com.ecommerce.store.domain.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Order information response")
public record OrderResponse(
        @Schema(description = "Order unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Buyer's email address", example = "customer@example.com")
        String buyerEmail,

        @Schema(description = "Order status", example = "CONFIRMED")
        OrderStatus status,

        @Schema(description = "List of order items")
        List<OrderItemResponse> items,

        @Schema(description = "Total order value", example = "1999.98")
        BigDecimal totalPrice,

        @Schema(description = "Timestamp when the order was placed")
        Instant createdAt,

        @Schema(description = "Timestamp when the order was last updated")
        Instant updatedAt
) {
}
