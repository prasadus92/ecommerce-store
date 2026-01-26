package com.ecommerce.store.integration;

import com.ecommerce.store.api.dto.request.CreateProductRequest;
import com.ecommerce.store.api.dto.request.UpdateProductRequest;
import com.ecommerce.store.domain.entity.Product;
import com.ecommerce.store.domain.repository.ProductRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Product API Integration Tests")
class ProductIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /products")
    class GetProducts {

        @Test
        @DisplayName("should return empty page when no products exist")
        void shouldReturnEmptyPage() {
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .get("/products")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(0))
                    .body("totalElements", equalTo(0))
                    .body("first", equalTo(true))
                    .body("last", equalTo(true));
        }

        @Test
        @DisplayName("should return paginated products")
        void shouldReturnPaginatedProducts() {
            // Given
            createProduct("Product 1", "SKU-001", BigDecimal.valueOf(99.99));
            createProduct("Product 2", "SKU-002", BigDecimal.valueOf(149.99));
            createProduct("Product 3", "SKU-003", BigDecimal.valueOf(199.99));

            // When & Then
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("page", 0)
                    .queryParam("size", 2)
            .when()
                    .get("/products")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(2))
                    .body("totalElements", equalTo(3))
                    .body("totalPages", equalTo(2))
                    .body("first", equalTo(true))
                    .body("last", equalTo(false));
        }

        @Test
        @DisplayName("should filter active products only")
        void shouldFilterActiveProducts() {
            // Given
            createProduct("Active Product", "SKU-ACTIVE", BigDecimal.valueOf(99.99), true);
            createProduct("Inactive Product", "SKU-INACTIVE", BigDecimal.valueOf(149.99), false);

            // When & Then
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("activeOnly", true)
            .when()
                    .get("/products")
            .then()
                    .statusCode(200)
                    .body("content", hasSize(1))
                    .body("content[0].name", equalTo("Active Product"));
        }
    }

    @Nested
    @DisplayName("GET /products/{id}")
    class GetProductById {

        @Test
        @DisplayName("should return product when exists")
        void shouldReturnProduct() {
            // Given
            Product product = createProduct("Test Product", "SKU-TEST", BigDecimal.valueOf(99.99));

            // When & Then
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .get("/products/{id}", product.getId())
            .then()
                    .statusCode(200)
                    .body("id", equalTo(product.getId().toString()))
                    .body("name", equalTo("Test Product"))
                    .body("sku", equalTo("SKU-TEST"))
                    .body("price", equalTo(99.99f))
                    .body("active", equalTo(true));
        }

        @Test
        @DisplayName("should return 404 when product not found")
        void shouldReturn404WhenNotFound() {
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .get("/products/{id}", "00000000-0000-0000-0000-000000000000")
            .then()
                    .statusCode(404)
                    .body("title", equalTo("Resource Not Found"))
                    .body("detail", containsString("Product not found"));
        }
    }

    @Nested
    @DisplayName("POST /products")
    class CreateProduct {

        @Test
        @DisplayName("should create product with valid data")
        void shouldCreateProduct() {
            CreateProductRequest request = new CreateProductRequest(
                    "New Product",
                    "Product description",
                    BigDecimal.valueOf(199.99),
                    "SKU-NEW"
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .post("/products")
            .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("name", equalTo("New Product"))
                    .body("description", equalTo("Product description"))
                    .body("price", equalTo(199.99f))
                    .body("sku", equalTo("SKU-NEW"))
                    .body("active", equalTo(true))
                    .body("createdAt", notNullValue());
        }

        @Test
        @DisplayName("should return 400 when name is missing")
        void shouldReturn400WhenNameMissing() {
            CreateProductRequest request = new CreateProductRequest(
                    null,
                    "Description",
                    BigDecimal.valueOf(99.99),
                    "SKU"
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .post("/products")
            .then()
                    .statusCode(400)
                    .body("title", equalTo("Validation Error"))
                    .body("errors.name", notNullValue());
        }

        @Test
        @DisplayName("should return 400 when price is negative")
        void shouldReturn400WhenPriceNegative() {
            CreateProductRequest request = new CreateProductRequest(
                    "Product",
                    "Description",
                    BigDecimal.valueOf(-10.00),
                    "SKU"
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .post("/products")
            .then()
                    .statusCode(400)
                    .body("title", equalTo("Validation Error"))
                    .body("errors.price", notNullValue());
        }

        @Test
        @DisplayName("should return 409 when SKU already exists")
        void shouldReturn409WhenDuplicateSku() {
            // Given
            createProduct("Existing Product", "DUPLICATE-SKU", BigDecimal.valueOf(99.99));

            CreateProductRequest request = new CreateProductRequest(
                    "New Product",
                    "Description",
                    BigDecimal.valueOf(199.99),
                    "DUPLICATE-SKU"
            );

            // When & Then
            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .post("/products")
            .then()
                    .statusCode(409)
                    .body("title", equalTo("Duplicate Resource"))
                    .body("detail", containsString("sku"));
        }
    }

    @Nested
    @DisplayName("PUT /products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("should update product with valid data")
        void shouldUpdateProduct() {
            // Given
            Product product = createProduct("Original Name", "SKU-UPDATE", BigDecimal.valueOf(99.99));

            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Name",
                    "Updated description",
                    BigDecimal.valueOf(149.99),
                    null,
                    null
            );

            // When & Then
            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .put("/products/{id}", product.getId())
            .then()
                    .statusCode(200)
                    .body("name", equalTo("Updated Name"))
                    .body("description", equalTo("Updated description"))
                    .body("price", equalTo(149.99f))
                    .body("sku", equalTo("SKU-UPDATE"));
        }

        @Test
        @DisplayName("should partially update product")
        void shouldPartiallyUpdateProduct() {
            // Given
            Product product = createProduct("Original Name", "SKU-PARTIAL", BigDecimal.valueOf(99.99));

            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Name Only",
                    null,
                    null,
                    null,
                    null
            );

            // When & Then
            given()
                    .contentType(ContentType.JSON)
                    .body(request)
            .when()
                    .put("/products/{id}", product.getId())
            .then()
                    .statusCode(200)
                    .body("name", equalTo("Updated Name Only"))
                    .body("price", equalTo(99.99f));
        }
    }

    @Nested
    @DisplayName("DELETE /products/{id}")
    class DeactivateProduct {

        @Test
        @DisplayName("should deactivate product")
        void shouldDeactivateProduct() {
            // Given
            Product product = createProduct("To Deactivate", "SKU-DEACTIVATE", BigDecimal.valueOf(99.99));

            // When & Then
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .delete("/products/{id}", product.getId())
            .then()
                    .statusCode(204);

            // Verify product is deactivated
            given()
                    .contentType(ContentType.JSON)
            .when()
                    .get("/products/{id}", product.getId())
            .then()
                    .statusCode(200)
                    .body("active", equalTo(false));
        }
    }

    private Product createProduct(String name, String sku, BigDecimal price) {
        return createProduct(name, sku, price, true);
    }

    private Product createProduct(String name, String sku, BigDecimal price, boolean active) {
        Product product = Product.builder()
                .name(name)
                .sku(sku)
                .price(price)
                .active(active)
                .build();
        return productRepository.save(product);
    }
}
