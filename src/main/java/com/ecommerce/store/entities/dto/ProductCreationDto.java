package com.ecommerce.store.entities.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@ApiModel (description = "The Product Creation DTO")
@Value
@Builder
public class ProductCreationDto {

    @NotEmpty
    private String name;

    private String description;

    @NotNull
    @Positive
    private Double price;
}
