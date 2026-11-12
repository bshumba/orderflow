
package com.brianshumba.orderflow.customer.application.service;

import com.brianshumba.orderflow.customer.application.dto.CreateCustomerRequest;
import com.brianshumba.orderflow.customer.application.dto.UpdateCustomerRequest;
import com.brianshumba.orderflow.customer.domain.exception.CustomerNotFoundException;
import com.brianshumba.orderflow.customer.domain.exception.DuplicateCustomerEmailException;
import com.brianshumba.orderflow.customer.domain.model.Customer;
import com.brianshumba.orderflow.customer.domain.repository.CustomerRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = Objects.requireNonNull(
            repository,
            "CustomerRepository cannot be null"
        );
    }

    public Customer createCustomer(CreateCustomerRequest request) {

        Objects.requireNonNull(request, "CreateCustomerRequest cannot be null");

        Customer customer = new Customer(
            UUID.randomUUID(),
            request.firstName(),
            request.lastName(),
            request.email(),
            request.phone()
        );

        if (repository.existsByEmail(customer.getEmail())) {
            throw new DuplicateCustomerEmailException(customer.getEmail());
        }

        repository.save(customer);

        return customer;
    }

    public Customer getCustomerById(UUID id) {

        return repository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public Customer getCustomerByEmail(String email) {

        return repository.findByEmail(email)
            .orElseThrow(() -> new CustomerNotFoundException(
                email
            ));
    }

    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    public Customer updateCustomer(UUID id, UpdateCustomerRequest request) {

        Objects.requireNonNull(request, "UpdateCustomerRequest cannot be null");

        Customer customer = getCustomerById(id);

        customer.updateDetails(
            request.firstName(),
            request.lastName(),
            request.phone()
        );

        repository.save(customer);

        return customer;
    }

    public Customer deactivateCustomer(UUID id) {

        Customer customer = getCustomerById(id);

        customer.deactivate();

        repository.save(customer);

        return customer;
    }
}
