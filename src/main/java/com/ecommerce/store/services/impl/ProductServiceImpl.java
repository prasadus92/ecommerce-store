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
import java.util.Optional;

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
        Optional<Product> existed = repository.findById(productDto.getId());
        if (existed.isPresent()) {
            Product existingProduct = existed.get();
            return repository.save(Product.builder()
                                          .id(productDto.getId())
                                          .name(StringUtils.isEmpty(productDto.getName()) ? existingProduct.getName() : productDto.getName())
                                          .description(
                                              StringUtils.isEmpty(productDto.getDescription()) ? existingProduct.getDescription() : productDto.getDescription())
                                          .price((productDto.getPrice() == null) ? existingProduct.getPrice()
                                                                                 : productDto.getPrice())
                                          .build());
        }

        throw new ProductNotExistsException("Product doesn't exist in the system to update");
    }

    // ToDo: Add pagination
    @Override
    public List<Product> getAllProducts() {
        return repository.findAll();
    }
}
