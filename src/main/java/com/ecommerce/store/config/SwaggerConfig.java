package com.ecommerce.store.config;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                                                      .apis(RequestHandlerSelectors.any())
                                                      .paths(PathSelectors.ant("/api/v1/**"))
                                                      .build().apiInfo(apiEndPointsInfo());
    }

    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("Online Store REST API")
                                   .description("CRUD service for an Online (E-commerce) Store")
                                   .contact(new Contact("Prasad Subrahmanya", "https://github.com/prasadus92", "prasadus92@gmail.com"))
                                   .license("MIT License")
                                   .licenseUrl("https://opensource.org/licenses/MIT")
                                   .build();
    }
}
