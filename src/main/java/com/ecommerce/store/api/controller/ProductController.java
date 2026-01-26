package com.ecommerce.store.api.controller;

import com.ecommerce.store.api.dto.request.CreateProductRequest;
import com.ecommerce.store.api.dto.request.UpdateProductRequest;
import com.ecommerce.store.api.dto.response.PageResponse;
import com.ecommerce.store.api.dto.response.ProductResponse;
import com.ecommerce.store.domain.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product management API")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves a paginated list of all products")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by active status only") @RequestParam(defaultValue = "false") boolean activeOnly) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (activeOnly) {
            return ResponseEntity.ok(PageResponse.from(productService.getActiveProducts(pageable)));
        }
        return ResponseEntity.ok(PageResponse.from(productService.getAllProducts(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Creates a new product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Product with same SKU already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Concurrent modification or duplicate SKU",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate product", description = "Soft-deletes a product by deactivating it")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> deactivateProduct(
            @Parameter(description = "Product ID") @PathVariable UUID id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}
