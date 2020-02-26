package com.ecommerce.store.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.store.entities.dao.Order;
import com.ecommerce.store.entities.dto.OrderDto;
import com.ecommerce.store.services.OrderService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping ("/api/v1/orders")
@Api (value = "Order Controller", description = "Available operations on Orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @ApiOperation (value = "Place a new Order", response = ResponseEntity.class)
    @ApiResponses ({
        @ApiResponse (code = 201, message = "Created"),
        @ApiResponse (code = 400, message = "Bad Request"),
        @ApiResponse (code = 403, message = "Forbidden")
    })
    @PostMapping (produces = "application/json")
    @ResponseBody
    public ResponseEntity placeOrder(@RequestBody @Valid OrderDto order) {
        log.info("### New Order creation request - {}", order);
        Order created = orderService.placeOrder(order);
        return new ResponseEntity(created, HttpStatus.CREATED);
    }

    @ApiOperation (value = "Fetch a list of Orders within a time period", response = List.class)
    @ApiResponses ({
        @ApiResponse (code = 200, message = "OK"),
        @ApiResponse (code = 400, message = "Bad Request"),
        @ApiResponse (code = 403, message = "Forbidden")
    })
    @GetMapping (produces = "application/json")
    public List<Order> getOrdersByPeriod(@RequestParam @DateTimeFormat (iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                         @RequestParam @DateTimeFormat (iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("### Fetching all orders between {} and {}", start, end);
        return orderService.getOrdersByPeriod(start, end);
    }
}
