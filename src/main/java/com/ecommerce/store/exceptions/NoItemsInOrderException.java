package com.ecommerce.store.exceptions;

public class NoItemsInOrderException extends RuntimeException {

    public NoItemsInOrderException(String message) {
        super(message);
    }
}
