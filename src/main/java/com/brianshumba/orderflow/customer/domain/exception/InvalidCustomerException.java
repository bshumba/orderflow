package com.brianshumba.orderflow.customer.domain.exception;

public class InvalidCustomerException extends IllegalArgumentException {

    public InvalidCustomerException(String message) {
        super(message);
    }
}
