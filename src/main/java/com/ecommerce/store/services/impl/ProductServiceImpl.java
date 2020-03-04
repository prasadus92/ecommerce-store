package com.ecommerce.store.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.ecommerce.store.entities.dao.Product;
import com.ecommerce.store.entities.dto.ProductCreationDto;
import com.ecommerce.store.entities.dto.ProductUpdationDto;
import com.ecommerce.store.exceptions.ProductNotExistsException;
import com.ecommerce.store.repositories.ProductRepository;
import com.ecommerce.store.services.ProductService;

import java.util.List;

import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.hasLength;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    public Product createProduct(ProductCreationDto productDto) {
        return repository.save(Product.builder()
                                      .name(productDto.getName())
                                      .description(productDto.getDescription())
                                      .price(productDto.getPrice())
                                      .build());
    }

    @Override
    public Product updateProduct(ProductUpdationDto productDto) {
        return repository
            .findById(productDto.getId())
            .map(existingProduct -> {
                modifyUpdatedFields(productDto, existingProduct);
                return repository.save(existingProduct);
                 }
            ).orElseThrow(() -> new ProductNotExistsException("Product doesn't exist in the system to update"));
    }

    private void modifyUpdatedFields(ProductUpdationDto productDto, Product existingProduct) {
        if (hasLength(productDto.getName())) {
            existingProduct.setName(productDto.getName());
        }
        if (hasLength(productDto.getDescription())) {
            existingProduct.setDescription(productDto.getDescription());
        }
        if (nonNull(productDto.getPrice())) {
            existingProduct.setPrice(productDto.getPrice());
        }
    }

    // ToDo: Add pagination
    @Override
    public List<Product> getAllProducts() {
        return repository.findAll();
    }
}
