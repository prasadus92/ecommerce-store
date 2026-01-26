package com.ecommerce.store.api.mapper;

import com.ecommerce.store.api.dto.response.OrderItemResponse;
import com.ecommerce.store.api.dto.response.OrderResponse;
import com.ecommerce.store.domain.entity.Order;
import com.ecommerce.store.domain.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalPrice", expression = "java(order.getTotalPrice())")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "subtotal", expression = "java(item.getSubtotal())")
    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponseList(Set<OrderItem> items);
}
