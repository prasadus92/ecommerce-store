package com.ecommerce.store.domain.exception;

public class DuplicateResourceException extends RuntimeException {

    private final String resourceType;
    private final String field;
    private final String value;

    public DuplicateResourceException(String resourceType, String field, String value) {
        super("%s already exists with %s: %s".formatted(resourceType, field, value));
        this.resourceType = resourceType;
        this.field = field;
        this.value = value;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
