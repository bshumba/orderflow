package com.brianshumba.orderflow.customer.application.dto;

public record UpdateCustomerRequest(
    String firstName,
    String lastName,
    String phone
) {}
