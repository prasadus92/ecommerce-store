# E-commerce Application

[![Build Status](https://travis-ci.com/prasadus92/registration-service.svg?branch=master)](https://travis-ci.com/prasadus92/ecommerce-store) [![codecov](https://codecov.io/gh/patrykkrawczyk/TDDAndDesignPatternsExample/branch/master/graph/badge.svg)](https://codecov.io/gh/prasadus92/ecommerce-store) [![Maintainability](https://api.codeclimate.com/v1/badges/9ef32de1cfdbcc97b1f0/maintainability)](https://codeclimate.com/github/prasadus92/ecommerce-store/maintainability)

## Problem Statement

Create a tiny REST/JSON web service in Java using Spring Boot (RestController) with an API that
supports basic Products' CRUD:

- Create a new Product

- Get a list of all Products

- Update a Product

The API should also support:

- Placing an Order

- Retrieving all orders within a given time period

A product should have a name and some representation of its price.

Each order should be recorded and have a list of products. It should also have the buyer’s e-mail, and the
time the order was placed. The total value of the order should always be calculated, based on the prices
of the products in it.

It should be possible to change the product’s price, but this shouldn’t affect the total value of orders which
have already been placed.

### Requirements

- Implement your solution according to the above specification.

- Provide unit tests.

- Document your REST-API.

- Provide a storage solution for persisting the web service’s state.

- Have a way to run the service with its dependencies (database etc) locally. You can use either a simple script or docker or something else. It’s up to you.

## Solution Overview

### Tools Used

- Java 8

- Spring Boot v2.2.4.RELEASE

- PostgreSQL v10

- Maven

- Docker

- Travis (CI)

## Building the Application

As the project uses Maven as the build system, just run the below command in the project directory -

```bash
mvn clean install
```

The name of the generated JAR is `ecommerce-store-application.jar`.

To build the Dockerfile as well (make sure that the Docker daemon is running) -

```bash
mvn clean install dockerfile:build
```

*NOTE:* Maven is configured with `dockerfile-maven-plugin` for building the Docker image

### Running Tests

Run the below command -

```bash
mvn clean test
```

## Running the Application

### Using Maven

You could use Maven to run the Spring Boot application; here is the command to use -

```bash
mvn spring-boot:run
```

### Using the generated artifact

Once you build the Application, the artifacts will be generated and copied to `target` directory. Hence, using the JAR, we could start the Application this way -

```bash
java -jar ecommerce-store-application.jar
```

### Using Docker

If you have generated the Docker image in the build step, use the below command to run the Application on Docker -

```bash
docker-compose up
```

To tail the logs, just run -

```bash
docker-compose logs -f
```

*NOTE:* The Spring profile used while running with Docker is `production`. This requires PostgreSQL to be running, which is already configured in the Docker Compose file.

## Accessing the API Documentation

Swagger is included in the project. Hence, the API documentation is available at http://localhost:8080/swagger-ui.html. 

## Questions on Extensibility

1. You do not need to add authentication to your web service, but propose a protocol / method and
justify your choice.

We could use OAuth2 and JWT based authentication which is quite straightforward to implement by adding appropriate Spring Security dependencies. Also, this is convenient as there might be multiple clients using this service in a real-world scenario (https://www.toptal.com/spring/spring-boot-oauth2-jwt-rest-protection).
An alternate approach is to use an identity provider like Okta, Auth0 etc., if that's an allowed use-case.

2. How can you make the service redundant? What considerations should you do?

The concurrency part is handled using Optimistic Locking, specifically for the update of the Products. It would be ideal to add additional mechanisms like an "ETag" - "If-Match" header support.
Also, configuration (including passwords) is currently packaged along with the generated artifact; it would be better to use a separate Spring Cloud Config service in a scenario where the architecture is based on Microservices. 

## To Do

1. Try finding an alternative to using `wait-for-postgres.sh`; it currently requires PostgreSQL client to be installed on the Docker container.
2. Improve tests & coverage.
3. Add pagination to "fetch" APIs.
4. "User" is not included in the implemented model; that's something to be considered in a real-world scenario.
