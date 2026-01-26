package com.ecommerce.store.api.controller;

import com.ecommerce.store.api.dto.request.CreateOrderRequest;
import com.ecommerce.store.api.dto.response.OrderResponse;
import com.ecommerce.store.api.dto.response.PageResponse;
import com.ecommerce.store.domain.entity.OrderStatus;
import com.ecommerce.store.domain.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves a paginated list of orders with optional date filtering")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "Start date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter by buyer email")
            @RequestParam(required = false) String email,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(PageResponse.from(orderService.getOrdersByEmail(email, pageable)));
        }

        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(PageResponse.from(orderService.getOrdersByDateRange(startDate, endDate, pageable)));
        }

        return ResponseEntity.ok(PageResponse.from(orderService.getAllOrders(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID") @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    @Operation(summary = "Create order", description = "Creates a new order with the specified items")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or business rule violation",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Updates the status of an existing order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable UUID id,
            @Parameter(description = "New order status") @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an existing order if it hasn't been shipped")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel order",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
