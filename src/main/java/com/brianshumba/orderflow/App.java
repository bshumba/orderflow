package com.brianshumba.orderflow;

import java.math.BigDecimal;

import com.brianshumba.orderflow.customer.application.dto.CreateCustomerRequest;
import com.brianshumba.orderflow.customer.application.dto.UpdateCustomerRequest;
import com.brianshumba.orderflow.customer.application.service.CustomerService;
import com.brianshumba.orderflow.customer.domain.exception.DuplicateCustomerEmailException;
import com.brianshumba.orderflow.customer.domain.model.Customer;
import com.brianshumba.orderflow.customer.domain.repository.CustomerRepository;
import com.brianshumba.orderflow.customer.infrastructure.persistence.InMemoryCustomerRepository;
import com.brianshumba.orderflow.product.application.dto.CreateProductRequest;
import com.brianshumba.orderflow.product.application.service.InventoryService;
import com.brianshumba.orderflow.product.application.service.ProductService;
import com.brianshumba.orderflow.product.domain.exception.InsufficientStockException;
import com.brianshumba.orderflow.product.domain.exception.InvalidStockOperationException;
import com.brianshumba.orderflow.product.domain.exception.ProductNotFoundException;
import com.brianshumba.orderflow.product.domain.repository.InventoryRepository;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;
import com.brianshumba.orderflow.product.infrastructure.persistence.InMemoryInventoryRepository;
import com.brianshumba.orderflow.product.infrastructure.persistence.InMemoryProductRepository;

public class App {
    public static void main(String[] args) {

        ProductRepository productRepository =
            new InMemoryProductRepository();

        InventoryRepository inventoryRepository =
            new InMemoryInventoryRepository();

        ProductService productService =
            new ProductService(productRepository);

        InventoryService inventoryService =
            new InventoryService(
                inventoryRepository,
                productRepository
            );

        CustomerRepository customerRepository =
            new InMemoryCustomerRepository();

        CustomerService customerService =
            new CustomerService(customerRepository);

        /*
        Customer workflow exercise
        1. Create two customers with different email addresses.
        2. Find one customer by UUID.
        3. Find another customer using an uppercase email.
        4. Update one customer's name and phone.
        5. Deactivate one customer.
        6. Attempt to register a duplicate email and handle the exception.
         */

        try {
            // 1. Create two customers with different email addresses.
            CreateCustomerRequest customer1Request = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "123-456-7890"
            );

            CreateCustomerRequest customer2Request = new CreateCustomerRequest(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "098-765-4321"
            );

            Customer customer1 = customerService.createCustomer(customer1Request);
            Customer customer2 = customerService.createCustomer(customer2Request);

            // 2. Find one customer by UUID.
            var customer1ById = customerService.getCustomerById(customer1.getId());
            System.out.println("Customer 1 by ID: " + customer1ById);

            // 3. Find another customer using an uppercase email.
            var customer2ByEmail = customerService.getCustomerByEmail("JANE.SMITH@EXAMPLE.COM");
            System.out.println("Customer 2 by email: " + customer2ByEmail);

            // 4. Update one customer's name and phone.
            var updateRequest = new UpdateCustomerRequest(
                "Jane",
                "Doe",
                "111-222-3333"
            );
            var updatedCustomer2 = customerService.updateCustomer(customer2.getId(), updateRequest);
            System.out.println("Updated Customer 2: " + updatedCustomer2);

            // 5. Deactivate one customer.
            customerService.deactivateCustomer(customer1.getId());
            System.out.println("Deactivated Customer 1: " + customerService.getCustomerById(customer1.getId()));

            // 6. Attempt to register a duplicate email and handle the exception.
            CreateCustomerRequest customer3Request = new CreateCustomerRequest(
                "Sandra",
                "Jones",
                "jane.smith@example.com",
                "098-765-4320"
            );

            customerService.createCustomer(customer3Request);

        } catch(DuplicateCustomerEmailException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }
}
