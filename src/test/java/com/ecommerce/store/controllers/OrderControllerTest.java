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
import com.ecommerce.store.entities.dto.OrderDto;
import com.ecommerce.store.entities.dto.OrderItemDto;
import com.ecommerce.store.entities.dto.ProductCreationDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ExtendWith (SpringExtension.class)
@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance (TestInstance.Lifecycle.PER_CLASS)
public class OrderControllerTest {

    @LocalServerPort
    protected int port;
    private String path;

    private String createdProductId;
    private ProductCreationDto created;

    @BeforeAll
    public void beforeTest() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        path = "api/v1/orders";
        created = ProductCreationDto.builder()
                                    .name("BatMobile")
                                    .description("The Batmobile is the fictional car driven by the superhero Batman")
                                    .price(70000.58)
                                    .build();

        //@formatter:off
        createdProductId = given()
            .contentType(ContentType.JSON)
            .body(created)
            .when()
            .post("api/v1/products")
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .extract()
            .path("id");
        //@formatter:on
    }

    @Test
    public void testPlaceOrderSucceedsForValidRequest() {
        OrderItemDto item = OrderItemDto.builder()
                                        .productId(UUID.fromString(createdProductId))
                                        .quantity(2)
                                        .build();
        OrderDto order = OrderDto.builder()
                                 .buyerEmail("bruce@wayne.com")
                                 .items(Collections.singletonList(item))
                                 .build();

        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(order)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .body("$", hasKey("id"))
            .body("id", notNullValue())
            .body("totalPrice", is(Float.valueOf("140001.16")));
        //@formatter:on
    }

    @Test
    public void testPlaceOrderFailsForRequestWithNoBuyerEmail() {
        OrderItemDto item = OrderItemDto.builder()
                                        .productId(UUID.fromString(createdProductId))
                                        .quantity(2)
                                        .build();
        OrderDto order = OrderDto.builder()
                                 .items(Collections.singletonList(item))
                                 .build();

        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(order)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
        //@formatter:on
    }

    @Test
    public void testPlaceOrderFailsForRequestWithNoItems() {
        OrderDto order = OrderDto.builder()
                                 .buyerEmail("bruce@wayne.com")
                                 .items(Collections.emptyList())
                                 .build();

        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(order)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .body("message", containsString("Order creation request doesn't contain any Products"));
        //@formatter:on
    }

    @Test
    public void testPlaceOrderFailsForRequestWithNonExistingProducts() {
        OrderItemDto item = OrderItemDto.builder()
                                        .productId(UUID.randomUUID())
                                        .quantity(2)
                                        .build();
        OrderDto order = OrderDto.builder()
                                 .buyerEmail("bruce@wayne.com")
                                 .items(Collections.singletonList(item))
                                 .build();

        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(order)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .body("message", containsString("One or more of the Products in the request doesn't exist"));
        //@formatter:on
    }

    // ToDo: This needs a fix, not a perfect test
    @Test
    public void testGetOrdersByPeriodSucceedsForValidInput() {
        OrderItemDto item = OrderItemDto.builder()
                                        .productId(UUID.fromString(createdProductId))
                                        .quantity(5)
                                        .build();
        OrderDto order = OrderDto.builder()
                                 .buyerEmail("batman@waynecorp.com")
                                 .items(Collections.singletonList(item))
                                 .build();

        //@formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(order)
            .when()
            .post(path)
            .then()
            .statusCode(HttpStatus.SC_CREATED);

        given()
            .contentType(ContentType.JSON)
            .queryParam("start", LocalDateTime.now().minusMinutes(2).toString())
            .queryParam("end", LocalDateTime.now().toString())
            .when()
            .get(path)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("size()", greaterThanOrEqualTo(1));
        //@formatter:on
    }
}
