
package com.brianshumba.orderflow.customer.infrastructure.persistence;

import com.brianshumba.orderflow.customer.domain.exception.DuplicateCustomerEmailException;
import com.brianshumba.orderflow.customer.domain.model.Customer;
import com.brianshumba.orderflow.customer.domain.repository.CustomerRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<UUID, Customer> customers = new HashMap<>();

    @Override
    public void save(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null");

        boolean emailAlreadyUsed = customers.values()
            .stream()
            .anyMatch(existing ->
                existing.getEmail().equals(customer.getEmail())
                && !existing.getId().equals(customer.getId())
            );

        if (emailAlreadyUsed) {
            throw new DuplicateCustomerEmailException(customer.getEmail());
        }

        customers.put(customer.getId(), customer);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public Optional<Customer> findByEmail(String email) {

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return customers.values()
            .stream()
            .filter(customer ->
                customer.getEmail().equals(normalizedEmail)
            )
            .findFirst();
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
