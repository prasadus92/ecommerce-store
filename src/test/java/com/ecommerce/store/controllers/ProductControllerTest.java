package com.ecommerce.store.controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.ecommerce.store.entities.dto.ProductCreationDto;
import com.ecommerce.store.entities.dto.ProductUpdationDto;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ExtendWith (SpringExtension.class)
@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance (TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerTest {

    @LocalServerPort
    protected int port;
    private String path;

    @BeforeAll
    public void beforeTest() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        path = "api/v1/products";
    }

    @Test
    public void testProductCreationSucceedsForValidRequest() {
        ProductCreationDto product = ProductCreationDto.builder()
                                                       .name("iPhone")
                                                       .description("The best selling mobile device from Apple")
                                                       .price(700.0)
                                                       .build();
        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_CREATED);
        //@formatter:on
    }

    @Test
    public void testProductCreationFailsWhenNoNameSpecified() {
        ProductCreationDto product = ProductCreationDto.builder()
                                                       .description("The best selling mobile device from Apple")
                                                       .price(700.0)
                                                       .build();
        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testProductCreationFailsWhenNoPriceSpecified() {
        ProductCreationDto product = ProductCreationDto.builder()
                                                       .name("iPhone")
                                                       .description("The best selling mobile device from Apple")
                                                       .build();
        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testProductCreationFailsWhenWrongPriceSpecified() {
        ProductCreationDto product = ProductCreationDto.builder()
                                                       .name("iPhone")
                                                       .description("The best selling mobile device from Apple")
                                                       .price(-700.0)
                                                       .build();
        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testProductUpdationSucceedsForValidRequest() {
        String createdProductId = createNewProduct();

        ProductUpdationDto updated = ProductUpdationDto.builder()
                                                       .id(UUID.fromString(createdProductId))
                                                       .name("iPhone 11")
                                                       .description("The brand new iPhone from Apple")
                                                       .price(1000.0)
                                                       .build();
        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(updated)
            .when()
            .put(path)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("id", equalTo(createdProductId))
            .body("name", equalTo(updated.getName()))
            .body("description", equalTo(updated.getDescription()))
            .body("price", is(Float.valueOf("1000.0")));
        //@formatter:on
    }

    @Test
    public void testProductUpdationSucceedsWhenPriceIsNull() {
        String createdProductId = createNewProduct();

        ProductUpdationDto updated = ProductUpdationDto.builder()
                                                       .id(UUID.fromString(createdProductId))
                                                       .name("iPhone 11")
                                                       .description("The brand new iPhone from Apple")
                                                       .price(null)
                                                       .build();
        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(updated)
            .when()
            .put(path)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("id", equalTo(createdProductId))
            .body("name", equalTo(updated.getName()))
            .body("description", equalTo(updated.getDescription()))
            .body("price", is(Float.valueOf("700.0")));
        //@formatter:on
    }

    @Test
    public void testOnlyPriceUpdationForProductSucceeds() {
        // Let's create a new Product first
        ProductCreationDto product = ProductCreationDto.builder()
                                                       .name("iPhone")
                                                       .description("The best selling mobile device from Apple")
                                                       .price(700.0)
                                                       .build();
        //@formatter:off
        String createdProductId = given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .extract()
            .path("id");
        //@formatter:on

        // Let's test the update call now

        ProductUpdationDto updated = ProductUpdationDto.builder()
                                                       .id(UUID.fromString(createdProductId))
                                                       .price(1000.0)
                                                       .build();
        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(updated)
            .when()
            .put(path)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("id", equalTo(createdProductId))
            .body("name", equalTo(product.getName()))
            .body("description", equalTo(product.getDescription()))
            .body("price", is(Float.valueOf("1000.0")));
        //@formatter:on
    }

    @Test
    public void testProductUpdationFailsForInvalidPrice() {
        String createdProductId = createNewProduct();

        ProductUpdationDto updated = ProductUpdationDto.builder()
                                                       .id(UUID.fromString(createdProductId))
                                                       .name("iPhone 11")
                                                       .description("The brand new iPhone from Apple")
                                                       .price(-1000.0)
                                                       .build();
        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(updated)
            .when()
            .put(path)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testProductUpdationFailsForNoId() {
        ProductUpdationDto updated = ProductUpdationDto.builder()
                                                       .name("iPhone 11")
                                                       .description("The brand new iPhone from Apple")
                                                       .price(1000.0)
                                                       .build();

        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(updated)
            .when()
            .put(path)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testProductUpdationFailsForNonExistingProduct() {
        ProductUpdationDto updated = ProductUpdationDto.builder()
                                                       .id(UUID.randomUUID())
                                                       .name("iPhone 11")
                                                       .description("The brand new iPhone from Apple")
                                                       .price(1000.0)
                                                       .build();
        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(updated)
            .when()
            .put(path)
            .then()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .body("message", containsString("Product doesn't exist in the system to update"));
        //@formatter:on
    }

    // ToDo: This needs a fix, not a perfect test
    @Test
    public void testFetchingProducts() {
        createNewProduct();

        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("size()", greaterThanOrEqualTo(1));
        //@formatter:on
    }

    private String createNewProduct() {
        ProductCreationDto product = ProductCreationDto.builder()
                                                       .name("iPhone")
                                                       .description("The best selling mobile device from Apple")
                                                       .price(700.0)
                                                       .build();

        //@formatter:off
        return given()
            .contentType(ContentType.JSON)
            .body(product)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .extract()
            .path("id");
        //@formatter:on
    }
}
