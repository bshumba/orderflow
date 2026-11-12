package com.brianshumba.orderflow.customer.application.dto;

public record CreateCustomerRequest(
    String firstName,
    String lastName,
    String email,
    String phone
) {}
