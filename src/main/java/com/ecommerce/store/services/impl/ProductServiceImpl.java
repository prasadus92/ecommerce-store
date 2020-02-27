package com.ecommerce.store.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.ecommerce.store.entities.dao.Product;
import com.ecommerce.store.entities.dto.ProductCreationDto;
import com.ecommerce.store.entities.dto.ProductUpdationDto;
import com.ecommerce.store.exceptions.ProductNotExistsException;
import com.ecommerce.store.repositories.ProductRepository;
import com.ecommerce.store.services.ProductService;

import java.util.List;

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
                checkAllFieldExists(productDto, existingProduct);
                return repository.save(existingProduct);
                 }
            ).orElseThrow(() -> new ProductNotExistsException("Product doesn't exist in the system to update"));
    }

    private void checkAllFieldExists(ProductUpdationDto productDto, Product existingProduct) {
        if (StringUtils.hasLength(productDto.getName())) {
            existingProduct.setName(productDto.getName());
        }
        if (StringUtils.hasLength(productDto.getDescription())) {
            existingProduct.setDescription(productDto.getDescription());
        }
        if (productDto.getPrice() != null) {
            existingProduct.setPrice(productDto.getPrice());
        }
    }

    // ToDo: Add pagination
    @Override
    public List<Product> getAllProducts() {
        return repository.findAll();
    }
}
