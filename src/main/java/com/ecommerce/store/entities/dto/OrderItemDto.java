package com.ecommerce.store.entities.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@ApiModel (description = "The OrderItem DTO")
@Value
@Builder
public class OrderItemDto {

    @NotNull
    private UUID productId;

    @NotNull
    private Integer quantity;
}
