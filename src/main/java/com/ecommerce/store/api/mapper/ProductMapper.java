package com.ecommerce.store.api.mapper;

import com.ecommerce.store.api.dto.request.CreateProductRequest;
import com.ecommerce.store.api.dto.request.UpdateProductRequest;
import com.ecommerce.store.api.dto.response.ProductResponse;
import com.ecommerce.store.domain.entity.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    Product toEntity(CreateProductRequest request);

    ProductResponse toResponse(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Product product, UpdateProductRequest request);
}
