package com.ecommerce.store.entities.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Value;

@ApiModel (description = "The Error DTO")
@Value
@Builder
public class ErrorDto {

    private String message;
}
