package com.ecommerce.store.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Order item information response")
public record OrderItemResponse(
        @Schema(description = "Order item unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID productId,

        @Schema(description = "Product name at time of order", example = "iPhone 15 Pro")
        String productName,

        @Schema(description = "Quantity ordered", example = "2")
        Integer quantity,

        @Schema(description = "Unit price at time of order", example = "999.99")
        BigDecimal unitPrice,

        @Schema(description = "Subtotal for this item (quantity x unit price)", example = "1999.98")
        BigDecimal subtotal
) {
}
