package com.ecommerce.store.api.exception;

import com.ecommerce.store.domain.exception.BusinessException;
import com.ecommerce.store.domain.exception.DuplicateResourceException;
import com.ecommerce.store.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TIMESTAMP = "timestamp";
    private static final String ERROR_CODE = "errorCode";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://api.ecommerce.com/errors/not-found"));
        problem.setProperty(TIMESTAMP, Instant.now());
        problem.setProperty("resourceType", ex.getResourceType());
        problem.setProperty("identifier", ex.getIdentifier());

        return problem;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Duplicate Resource");
        problem.setType(URI.create("https://api.ecommerce.com/errors/duplicate"));
        problem.setProperty(TIMESTAMP, Instant.now());
        problem.setProperty("resourceType", ex.getResourceType());
        problem.setProperty("field", ex.getField());

        return problem;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex) {
        log.warn("Business error: {} - {}", ex.getErrorCode(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Business Rule Violation");
        problem.setType(URI.create("https://api.ecommerce.com/errors/business"));
        problem.setProperty(TIMESTAMP, Instant.now());
        problem.setProperty(ERROR_CODE, ex.getErrorCode());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for the request"
        );
        problem.setTitle("Validation Error");
        problem.setType(URI.create("https://api.ecommerce.com/errors/validation"));
        problem.setProperty(TIMESTAMP, Instant.now());
        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLocking(OptimisticLockingFailureException ex) {
        log.warn("Optimistic locking failure: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The resource was modified by another request. Please retry."
        );
        problem.setTitle("Concurrent Modification");
        problem.setType(URI.create("https://api.ecommerce.com/errors/conflict"));
        problem.setProperty(TIMESTAMP, Instant.now());
        problem.setProperty(ERROR_CODE, "CONCURRENT_MODIFICATION");

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.ecommerce.com/errors/internal"));
        problem.setProperty(TIMESTAMP, Instant.now());

        return problem;
    }
}
