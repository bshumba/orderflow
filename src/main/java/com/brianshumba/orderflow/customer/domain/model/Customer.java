
package com.brianshumba.orderflow.customer.domain.model;

import com.brianshumba.orderflow.customer.domain.exception.InvalidCustomerException;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Customer {

    private final UUID id;
    private final String email;

    private String firstName;
    private String lastName;
    private String phone;
    private boolean active;

    public Customer(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone
    ) {
        this.id = Objects.requireNonNull(id, "Customer ID cannot be null");
        this.firstName = validateName(firstName);
        this.lastName = validateName(lastName);
        this.email = validateEmail(email);
        this.phone = validatePhone(phone);
        this.active = true;
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidCustomerException(
                "Customer name cannot be blank"
            );
        }

        return name.trim();
    }

    private static String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidCustomerException(
                "Customer email cannot be blank"
            );
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new InvalidCustomerException(
                "Invalid customer email address"
            );
        }

        return normalizedEmail;
    }

    private static String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new InvalidCustomerException(
                "Customer phone cannot be blank"
            );
        }

        return phone.trim();
    }

    public void updateDetails(
        String firstName,
        String lastName,
        String phone
    ) {
        String validatedFirstName = validateName(firstName);
        String validatedLastName = validateName(lastName);
        String validatedPhone = validatePhone(phone);

        this.firstName = validatedFirstName;
        this.lastName = validatedLastName;
        this.phone = validatedPhone;
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public String toString() {
        return "Customer{" +
            "id=" + id +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", phone=" + phone +
            ", active=" + active +
            '}';
    }
}
