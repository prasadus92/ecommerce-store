package com.ecommerce.store.domain.service;

import com.ecommerce.store.api.dto.request.CreateProductRequest;
import com.ecommerce.store.api.dto.request.UpdateProductRequest;
import com.ecommerce.store.api.dto.response.ProductResponse;
import com.ecommerce.store.api.mapper.ProductMapper;
import com.ecommerce.store.config.CacheConfig;
import com.ecommerce.store.domain.entity.Product;
import com.ecommerce.store.domain.exception.DuplicateResourceException;
import com.ecommerce.store.domain.exception.ResourceNotFoundException;
import com.ecommerce.store.domain.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Cacheable(value = CacheConfig.PRODUCTS_CACHE, key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products, page: {}", pageable);
        return productRepository.findAll(pageable)
                .map(productMapper::toResponse);
    }

    public Page<ProductResponse> getActiveProducts(Pageable pageable) {
        log.debug("Fetching active products, page: {}", pageable);
        return productRepository.findByActiveTrue(pageable)
                .map(productMapper::toResponse);
    }

    @Cacheable(value = CacheConfig.PRODUCT_CACHE, key = "#id")
    public ProductResponse getProductById(UUID id) {
        log.debug("Fetching product by id: {}", id);
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));
    }

    public Product getProductEntityById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCTS_CACHE, allEntries = true)
    })
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.name());

        if (request.sku() != null && productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product", "sku", request.sku());
        }

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);

        log.info("Product created with id: {}", savedProduct.getId());
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCTS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#id")
    })
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));

        if (request.sku() != null && !request.sku().equals(product.getSku())) {
            if (productRepository.existsBySku(request.sku())) {
                throw new DuplicateResourceException("Product", "sku", request.sku());
            }
        }

        productMapper.updateEntity(product, request);
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated: {}", updatedProduct.getId());
        return productMapper.toResponse(updatedProduct);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCTS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#id")
    })
    public void deactivateProduct(UUID id) {
        log.info("Deactivating product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));

        product.setActive(false);
        productRepository.save(product);

        log.info("Product deactivated: {}", id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCTS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.PRODUCT_CACHE, key = "#id")
    })
    public void deleteProduct(UUID id) {
        log.info("Deleting product: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id.toString());
        }

        productRepository.deleteById(id);
        log.info("Product deleted: {}", id);
    }
}
