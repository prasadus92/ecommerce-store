package com.ecommerce.store.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.ecommerce.store.entities.dao.Order;
import com.ecommerce.store.entities.dao.OrderItem;
import com.ecommerce.store.entities.dto.OrderDto;
import com.ecommerce.store.exceptions.NoItemsInOrderException;
import com.ecommerce.store.exceptions.ProductNotExistsException;
import com.ecommerce.store.repositories.OrderItemRepository;
import com.ecommerce.store.repositories.OrderRepository;
import com.ecommerce.store.repositories.ProductRepository;
import com.ecommerce.store.services.OrderService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    public Order placeOrder(OrderDto order) {
        checkIfItemsAreEmpty(order);

        Order created = orderRepository.save(Order.builder()
                                                  .buyerEmail(order.getBuyerEmail())
                                                  .build());
        log.info("### New order persisted");

        Set<OrderItem> items = new HashSet<>();

        order.getItems().forEach(item -> productRepository
            .findById(item.getProductId())
            .map(product -> items.add(orderItemRepository.save(
                OrderItem.builder()
                         .order(created)
                         .product(product)
                         .quantity(item.getQuantity())
                         .unitPrice(product.getPrice())
                         .build()))).orElseThrow(() -> new ProductNotExistsException("One or more of the Products in the request doesn't exist")));

        log.info("### All order items are persisted");
        created.setItems(items);

        return orderRepository.save(created);
    }

    private void checkIfItemsAreEmpty(OrderDto order) {
        if (isNull(order) || order.getItems().isEmpty()) {
            throw new NoItemsInOrderException("Order creation request doesn't contain any Products");
        }
    }

    @Override
    public List<Order> getOrdersByPeriod(LocalDateTime start, LocalDateTime end) {
        log.info("### Fetching orders from the database");
        return orderRepository.findByCreatedAtBetween(start, end);
    }

    // ToDo: Add pagination
    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
