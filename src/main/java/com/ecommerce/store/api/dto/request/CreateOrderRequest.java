package com.ecommerce.store.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request payload for placing a new order")
public record CreateOrderRequest(
        @Schema(description = "Buyer's email address", example = "customer@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Buyer email is required")
        @Email(message = "Invalid email format")
        String buyerEmail,

        @Schema(description = "List of order items", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items
) {
}
