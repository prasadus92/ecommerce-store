package com.ecommerce.store.services;

import com.ecommerce.store.entities.dao.Order;
import com.ecommerce.store.entities.dto.OrderDto;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    Order placeOrder(OrderDto order);

    List<Order> getOrdersByPeriod(LocalDateTime start, LocalDateTime end);

    List<Order> findAll();

}
