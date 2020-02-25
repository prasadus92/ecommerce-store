package com.ecommerce.store.entities.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;
import com.ecommerce.store.validation.NullablePrice;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@ApiModel (description = "The Product Updation DTO")
@Value
@Builder
public class ProductUpdationDto {

    @NotNull
    private UUID id;

    private String name;

    private String description;

    @NullablePrice
    private Double price;
}
