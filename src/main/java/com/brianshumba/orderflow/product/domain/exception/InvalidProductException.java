package com.brianshumba.orderflow.product.domain.exception;

public class InvalidProductException
    extends IllegalArgumentException {

        public InvalidProductException(String message) {
            super(message);
        }
}
