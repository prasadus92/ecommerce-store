package com.ecommerce.store.domain.service;

import com.ecommerce.store.api.dto.request.CreateProductRequest;
import com.ecommerce.store.api.dto.request.UpdateProductRequest;
import com.ecommerce.store.api.dto.response.ProductResponse;
import com.ecommerce.store.api.mapper.ProductMapper;
import com.ecommerce.store.domain.entity.Product;
import com.ecommerce.store.domain.exception.DuplicateResourceException;
import com.ecommerce.store.domain.exception.ResourceNotFoundException;
import com.ecommerce.store.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductResponse testProductResponse;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .sku("TEST-SKU")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(0L)
                .build();

        testProductResponse = new ProductResponse(
                productId,
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(99.99),
                "TEST-SKU",
                true,
                Instant.now(),
                Instant.now()
        );
    }

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("should return paginated products")
        void shouldReturnPaginatedProducts() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
            when(productRepository.findAll(pageable)).thenReturn(productPage);
            when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

            // When
            Page<ProductResponse> result = productService.getAllProducts(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("Test Product");
            verify(productRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("should return product when found")
        void shouldReturnProductWhenFound() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

            // When
            ProductResponse result = productService.getProductById(productId);

            // Then
            assertThat(result.id()).isEqualTo(productId);
            assertThat(result.name()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.getProductById(productId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product");
        }
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "New Product",
                    "Description",
                    BigDecimal.valueOf(149.99),
                    "NEW-SKU"
            );
            when(productRepository.existsBySku("NEW-SKU")).thenReturn(false);
            when(productMapper.toEntity(request)).thenReturn(testProduct);
            when(productRepository.save(testProduct)).thenReturn(testProduct);
            when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

            // When
            ProductResponse result = productService.createProduct(request);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when SKU exists")
        void shouldThrowExceptionWhenSkuExists() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "New Product",
                    "Description",
                    BigDecimal.valueOf(149.99),
                    "EXISTING-SKU"
            );
            when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("sku");
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("should update product successfully")
        void shouldUpdateProductSuccessfully() {
            // Given
            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Name",
                    "Updated Description",
                    BigDecimal.valueOf(199.99),
                    null,
                    null
            );
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(testProduct)).thenReturn(testProduct);
            when(productMapper.toResponse(testProduct)).thenReturn(testProductResponse);

            // When
            ProductResponse result = productService.updateProduct(productId, request);

            // Then
            assertThat(result).isNotNull();
            verify(productMapper).updateEntity(testProduct, request);
            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Name",
                    null, null, null, null
            );
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(productId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deactivateProduct")
    class DeactivateProduct {

        @Test
        @DisplayName("should deactivate product successfully")
        void shouldDeactivateProductSuccessfully() {
            // Given
            when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.deactivateProduct(productId);

            // Then
            assertThat(testProduct.isActive()).isFalse();
            verify(productRepository).save(testProduct);
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("should delete product successfully")
        void shouldDeleteProductSuccessfully() {
            // Given
            when(productRepository.existsById(productId)).thenReturn(true);

            // When
            productService.deleteProduct(productId);

            // Then
            verify(productRepository).deleteById(productId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            // Given
            when(productRepository.existsById(productId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(productId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
