package com.ecommerce.store.entities.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel (description = "The Order DTO")
@Value
@Builder
public class OrderDto {

    @NotNull
    List<OrderItemDto> items;

    @NotEmpty
    private String buyerEmail;
}
