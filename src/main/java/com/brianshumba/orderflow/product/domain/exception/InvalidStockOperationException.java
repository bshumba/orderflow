package com.brianshumba.orderflow.product.domain.exception;

public class InvalidStockOperationException extends RuntimeException {

    public InvalidStockOperationException(String message) {
        super(message);
    }

}
