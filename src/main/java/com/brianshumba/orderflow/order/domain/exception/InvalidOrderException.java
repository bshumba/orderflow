package com.brianshumba.orderflow.order.domain.exception;

public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
