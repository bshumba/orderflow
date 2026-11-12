
package com.brianshumba.orderflow.customer.application.service;

import com.brianshumba.orderflow.customer.application.dto.CreateCustomerRequest;
import com.brianshumba.orderflow.customer.application.dto.UpdateCustomerRequest;
import com.brianshumba.orderflow.customer.domain.exception.DuplicateCustomerEmailException;
import com.brianshumba.orderflow.customer.domain.model.Customer;
import com.brianshumba.orderflow.customer.infrastructure.persistence.InMemoryCustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(
            new InMemoryCustomerRepository()
        );
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        Customer customer = service.createCustomer(
            new CreateCustomerRequest(
                "John",
                "Doe",
                "john@example.com",
                "0771234567"
            )
        );

        assertNotNull(customer.getId());
        assertTrue(customer.isActive());
        assertEquals("john@example.com", customer.getEmail());
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {
        service.createCustomer(
            new CreateCustomerRequest(
                "John", "Doe",
                "john@example.com", "0771234567"
            )
        );

        assertThrows(
            DuplicateCustomerEmailException.class,
            () -> service.createCustomer(
                new CreateCustomerRequest(
                    "Jane", "Doe",
                    "JOHN@EXAMPLE.COM", "0779876543"
                )
            )
        );
    }

    @Test
    void shouldPreserveDetailsWhenUpdateFails() {
        Customer customer = service.createCustomer(
            new CreateCustomerRequest(
                "John", "Doe",
                "john@example.com", "0771234567"
            )
        );

        assertThrows(
            RuntimeException.class,
            () -> service.updateCustomer(
                customer.getId(),
                new UpdateCustomerRequest(
                    "Updated", "", "0779999999"
                )
            )
        );

        Customer stored = service.getCustomerById(customer.getId());

        assertEquals("John", stored.getFirstName());
        assertEquals("Doe", stored.getLastName());
        assertEquals("0771234567", stored.getPhone());
    }
}
