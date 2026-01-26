package com.ecommerce.store.integration;

import com.ecommerce.store.api.dto.request.CreateOrderRequest;
import com.ecommerce.store.api.dto.request.OrderItemRequest;
import com.ecommerce.store.domain.entity.Order;
import com.ecommerce.store.domain.entity.OrderStatus;
import com.ecommerce.store.domain.entity.Product;
import com.ecommerce.store.domain.repository.OrderRepository;
import com.ecommerce.store.domain.repository.ProductRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Order API Integration Tests")
class OrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void cleanUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();

        testProduct = productRepository.save(Product.builder()
                .name("Test Product")
                .sku("TEST-SKU")
                .price(BigDecimal.valueOf(99.99))
                .active(true)
                .build());
    }

    @Nested
    @DisplayName("GET /orders")
    class GetOrders {

        @Test
        @DisplayName("should return empty page when no orders exist")
        void shouldReturnEmptyPage() {
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .get("/orders")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(0))
                    .body("totalElements", equalTo(0));
        }

        @Test
        @DisplayName("should return paginated orders")
        void shouldReturnPaginatedOrders() {
            // Given
            createOrder("customer1@example.com");
            createOrder("customer2@example.com");
            createOrder("customer3@example.com");

            // When & Then
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("page", 0)
                    .queryParam("size", 2)
            .when()
                    .get("/orders")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(2))
                    .body("totalElements", equalTo(3))
                    .body("totalPages", equalTo(2));
        }

        @Test
        @DisplayName("should filter orders by email")
        void shouldFilterByEmail() {
            // Given
            createOrder("target@example.com");
            createOrder("other@example.com");

            // When & Then
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("email", "target@example.com")
            .when()
                    .get("/orders")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(1))
                    .body("content[0].buyerEmail", equalTo("target@example.com"));
        }
    }

    @Nested
    @DisplayName("GET /orders/{id}")
    class GetOrderById {

        @Test
        @DisplayName("should return order when exists")
        void shouldReturnOrder() {
            // Given
            Order order = createOrder("customer@example.com");

            // When & Then
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .get("/orders/{id}", order.getId())
            .then()
                    .statusCode(200)
                    .body("id", equalTo(order.getId().toString()))
                    .body("buyerEmail", equalTo("customer@example.com"))
                    .body("status", equalTo("PENDING"))
                    .body("items", notNullValue())
                    .body("totalPrice", notNullValue());
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() {
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .get("/orders/{id}", "00000000-0000-0000-0000-000000000000")
            .then()
                    .statusCode(404)
                    .body("title", equalTo("Resource Not Found"));
        }
    }

    @Nested
    @DisplayName("POST /orders")
    class CreateOrder {

        @Test
        @DisplayName("should create order with valid data")
        void shouldCreateOrder() {
            CreateOrderRequest request = new CreateOrderRequest(
                    "customer@example.com",
                    List.of(new OrderItemRequest(testProduct.getId(), 2))
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .post("/orders")
            .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("buyerEmail", equalTo("customer@example.com"))
                    .body("status", equalTo("PENDING"))
                    .body("items", hasSize(1))
                    .body("items[0].productId", equalTo(testProduct.getId().toString()))
                    .body("items[0].quantity", equalTo(2))
                    .body("items[0].unitPrice", equalTo(99.99f))
                    .body("totalPrice", equalTo(199.98f));
        }

        @Test
        @DisplayName("should return 400 when email is invalid")
        void shouldReturn400WhenInvalidEmail() {
            CreateOrderRequest request = new CreateOrderRequest(
                    "invalid-email",
                    List.of(new OrderItemRequest(testProduct.getId(), 1))
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .post("/orders")
            .then()
                    .statusCode(400)
                    .body("title", equalTo("Validation Error"))
                    .body("errors.buyerEmail", notNullValue());
        }

        @Test
        @DisplayName("should return 400 when items list is empty")
        void shouldReturn400WhenNoItems() {
            CreateOrderRequest request = new CreateOrderRequest(
                    "customer@example.com",
                    List.of()
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .post("/orders")
            .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() {
            CreateOrderRequest request = new CreateOrderRequest(
                    "customer@example.com",
                    List.of(new OrderItemRequest(UUID.randomUUID(), 1))
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .post("/orders")
            .then()
                    .statusCode(404)
                    .body("title", equalTo("Resource Not Found"));
        }

        @Test
        @DisplayName("should return 400 when product is inactive")
        void shouldReturn400WhenProductInactive() {
            // Given
            Product inactiveProduct = productRepository.save(Product.builder()
                    .name("Inactive Product")
                    .sku("INACTIVE-SKU")
                    .price(BigDecimal.valueOf(49.99))
                    .active(false)
                    .build());

            CreateOrderRequest request = new CreateOrderRequest(
                    "customer@example.com",
                    List.of(new OrderItemRequest(inactiveProduct.getId(), 1))
            );

            // When & Then
            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .post("/orders")
            .then()
                    .statusCode(400)
                    .body("title", equalTo("Business Rule Violation"))
                    .body("detail", containsString("not available"));
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id}/status")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should update order status")
        void shouldUpdateStatus() {
            // Given
            Order order = createOrder("customer@example.com");

            // When & Then
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("status", "CONFIRMED")
            .when()
                    .patch("/orders/{id}/status", order.getId())
            .then()
                    .statusCode(200)
                    .body("status", equalTo("CONFIRMED"));
        }

        @Test
        @DisplayName("should return 400 for invalid status transition")
        void shouldReturn400ForInvalidTransition() {
            // Given
            Order order = createOrder("customer@example.com");
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);

            // When & Then - Cannot transition from DELIVERED
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("status", "PENDING")
            .when()
                    .patch("/orders/{id}/status", order.getId())
            .then()
                    .statusCode(400)
                    .body("title", equalTo("Business Rule Violation"));
        }
    }

    @Nested
    @DisplayName("POST /orders/{id}/cancel")
    class CancelOrder {

        @Test
        @DisplayName("should cancel pending order")
        void shouldCancelPendingOrder() {
            // Given
            Order order = createOrder("customer@example.com");

            // When & Then
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .post("/orders/{id}/cancel", order.getId())
            .then()
                    .statusCode(200)
                    .body("status", equalTo("CANCELLED"));
        }

        @Test
        @DisplayName("should return 400 when cancelling shipped order")
        void shouldReturn400WhenCancellingShippedOrder() {
            // Given
            Order order = createOrder("customer@example.com");
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);

            // When & Then
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .post("/orders/{id}/cancel", order.getId())
            .then()
                    .statusCode(400)
                    .body("title", equalTo("Business Rule Violation"))
                    .body("detail", containsString("Cannot cancel"));
        }
    }

    private Order createOrder(String email) {
        Order order = Order.builder()
                .buyerEmail(email)
                .status(OrderStatus.PENDING)
                .build();
        return orderRepository.save(order);
    }
}
