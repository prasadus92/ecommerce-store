package com.ecommerce.store.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.ecommerce.store.entities.dao.Product;
import com.ecommerce.store.entities.dto.ProductCreationDto;
import com.ecommerce.store.entities.dto.ProductUpdationDto;
import com.ecommerce.store.exceptions.ProductNotExistsException;
import com.ecommerce.store.repositories.ProductRepository;
import com.ecommerce.store.services.impl.ProductServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith (SpringExtension.class)
public class ProductServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Captor
    private ArgumentCaptor<Product> productArgumentCaptor;

    @Captor
    private ArgumentCaptor<UUID> productIdArgumentCaptor;

    @Test
    public void testProductCreationSucceeds() {
        // Given
        Product product = Product.builder()
                                 .id(UUID.randomUUID())
                                 .name("Batarang")
                                 .description("Small but deadliest weapon of Batman")
                                 .price(8000.5)
                                 .build();
        when(productRepository.save(product)).thenReturn(product);

        // When
        productService
            .createProduct(ProductCreationDto.builder()
                                             .name("Batarang")
                                             .description("Small but deadliest weapon of Batman")
                                             .price(8000.5)
                                             .build());

        // Then
        verify(productRepository, times(1)).save(productArgumentCaptor.capture());
        assertThat(productArgumentCaptor.getValue(), hasProperty("name", equalTo("Batarang")));
        assertThat(productArgumentCaptor.getValue(), hasProperty("description", equalTo("Small but deadliest weapon of Batman")));
        assertThat(productArgumentCaptor.getValue(), hasProperty("price", equalTo(8000.5)));
    }

    @Test
    public void testProductUpdationSucceeds() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                                 .id(productId)
                                 .name("Kryptonite Grenade Launcher")
                                 .description("The Grenade Launcher in Batman's style")
                                 .price(120000.98)
                                 .build();
        Product updated = Product.builder()
                                 .id(productId)
                                 .name("Grenade Launcher")
                                 .description("The Grenade Launcher in Batman's style")
                                 .price(130000.0)
                                 .build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(updated);

        // When
        Product returned = productService
            .updateProduct(ProductUpdationDto.builder()
                                             .id(productId)
                                             .name("Grenade Launcher")
                                             .price(130000.0)
                                             .build());

        // Then
        verify(productRepository, times(1)).findById(productIdArgumentCaptor.capture());
        assertThat(productIdArgumentCaptor.getValue(), equalTo(productId));
        verify(productRepository, times(1)).save(productArgumentCaptor.capture());
        assertThat(productArgumentCaptor.getValue(), hasProperty("name", equalTo("Grenade Launcher")));
        assertThat(productArgumentCaptor.getValue(), hasProperty("description", equalTo("The Grenade Launcher in Batman's style")));
        assertThat(productArgumentCaptor.getValue(), hasProperty("price", equalTo(130000.0)));
        assertThat(returned.getPrice(), equalTo(130000.0));
        assertThat(returned.getName(), equalTo("Grenade Launcher"));
    }

    @Test
    public void testProductUpdationThrowsExceptionForNonExistingProduct() {
        // Given
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        // When
        Exception exception = assertThrows(
            ProductNotExistsException.class,
            () -> productService
                .updateProduct(ProductUpdationDto.builder()
                                                 .id(UUID.randomUUID())
                                                 .name("Grenade Launcher")
                                                 .price(130000.0)
                                                 .build()));
        assertEquals(exception.getMessage(), "Product doesn't exist in the system to update");
    }

    @TestConfiguration
    static class ProductServiceTestContextConfiguration {

        @MockBean
        private ProductRepository productRepository;

        @Bean
        public ProductService productService() {
            return new ProductServiceImpl(productRepository);
        }
    }
}
