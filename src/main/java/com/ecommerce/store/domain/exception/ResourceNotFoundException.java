package com.ecommerce.store.domain.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String identifier;

    public ResourceNotFoundException(String resourceType, String identifier) {
        super("%s not found with identifier: %s".formatted(resourceType, identifier));
        this.resourceType = resourceType;
        this.identifier = identifier;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getIdentifier() {
        return identifier;
    }
}
