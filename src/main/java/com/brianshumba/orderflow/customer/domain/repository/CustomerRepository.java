
package com.brianshumba.orderflow.customer.domain.repository;

import com.brianshumba.orderflow.customer.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    void save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByEmail(String email);

    List<Customer> findAll();

    boolean existsByEmail(String email);
}
