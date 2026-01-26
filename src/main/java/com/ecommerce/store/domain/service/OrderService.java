package com.ecommerce.store.domain.service;

import com.ecommerce.store.api.dto.request.CreateOrderRequest;
import com.ecommerce.store.api.dto.request.OrderItemRequest;
import com.ecommerce.store.api.dto.response.OrderResponse;
import com.ecommerce.store.api.mapper.OrderMapper;
import com.ecommerce.store.domain.entity.Order;
import com.ecommerce.store.domain.entity.OrderItem;
import com.ecommerce.store.domain.entity.OrderStatus;
import com.ecommerce.store.domain.entity.Product;
import com.ecommerce.store.domain.exception.BusinessException;
import com.ecommerce.store.domain.exception.ResourceNotFoundException;
import com.ecommerce.store.domain.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, ProductService productService, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.orderMapper = orderMapper;
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders, page: {}", pageable);
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }

    public Page<OrderResponse> getOrdersByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Fetching orders between {} and {}", startDate, endDate);

        Instant startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        return orderRepository.findByCreatedAtBetween(startInstant, endInstant, pageable)
                .map(orderMapper::toResponse);
    }

    public Page<OrderResponse> getOrdersByEmail(String email, Pageable pageable) {
        log.debug("Fetching orders for email: {}", email);
        return orderRepository.findByBuyerEmailIgnoreCase(email, pageable)
                .map(orderMapper::toResponse);
    }

    public OrderResponse getOrderById(UUID id) {
        log.debug("Fetching order by id: {}", id);
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id.toString()));
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for: {}", request.buyerEmail());

        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException("EMPTY_ORDER", "Order must contain at least one item");
        }

        Order order = Order.builder()
                .buyerEmail(request.buyerEmail())
                .status(OrderStatus.PENDING)
                .build();

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productService.getProductEntityById(itemRequest.productId());

            if (!product.isActive()) {
                throw new BusinessException("INACTIVE_PRODUCT",
                        "Product '%s' is not available for ordering".formatted(product.getName()));
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {}, total: {}", savedOrder.getId(), savedOrder.getTotalPrice());

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID id, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id.toString()));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated to {}", id, newStatus);
        return orderMapper.toResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        log.info("Cancelling order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id.toString()));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("CANNOT_CANCEL",
                    "Cannot cancel order that has been shipped or delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled: {}", id);
        return orderMapper.toResponse(cancelledOrder);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        boolean valid = switch (current) {
            case PENDING -> target == OrderStatus.CONFIRMED || target == OrderStatus.CANCELLED;
            case CONFIRMED -> target == OrderStatus.PROCESSING || target == OrderStatus.CANCELLED;
            case PROCESSING -> target == OrderStatus.SHIPPED || target == OrderStatus.CANCELLED;
            case SHIPPED -> target == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!valid) {
            throw new BusinessException("INVALID_STATUS_TRANSITION",
                    "Cannot transition from %s to %s".formatted(current, target));
        }
    }
}
