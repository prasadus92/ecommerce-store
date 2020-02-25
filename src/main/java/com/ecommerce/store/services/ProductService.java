package com.ecommerce.store.services;

import com.ecommerce.store.entities.dao.Product;
import com.ecommerce.store.entities.dto.ProductCreationDto;
import com.ecommerce.store.entities.dto.ProductUpdationDto;

import java.util.List;

public interface ProductService {

    Product createProduct(ProductCreationDto productDto);

    Product updateProduct(ProductUpdationDto productDto);

    List<Product> getAllProducts();
}
