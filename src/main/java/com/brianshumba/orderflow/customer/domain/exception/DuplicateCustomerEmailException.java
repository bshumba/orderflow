package com.brianshumba.orderflow.customer.domain.exception;

public class DuplicateCustomerEmailException extends RuntimeException {

    public DuplicateCustomerEmailException(String email) {
        super("A customer with the email '" + email + "' already exists.");
    }

}
