package com.hospi.manage.features.account.dto;

import com.hospi.manage.features.account.enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AccountCreateFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        var form = new AccountCreateForm("John Doe", "john@example.com", "password123", "1234567890", Role.LEADER);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldPass_whenPhoneNull() {
        var form = new AccountCreateForm("John Doe", "john@example.com", "password123", null, Role.LEADER);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenFullNameBlank() {
        var form = new AccountCreateForm("", "john@example.com", "password123", null, Role.LEADER);
        Set<ConstraintViolation<AccountCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("fullName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenEmailBlank() {
        var form = new AccountCreateForm("John Doe", "", "password123", null, Role.LEADER);
        Set<ConstraintViolation<AccountCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        var form = new AccountCreateForm("John Doe", "not-an-email", "password123", null, Role.LEADER);
        Set<ConstraintViolation<AccountCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenPasswordBlank() {
        var form = new AccountCreateForm("John Doe", "john@example.com", "", null, Role.LEADER);
        Set<ConstraintViolation<AccountCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("password", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenRoleNull() {
        var form = new AccountCreateForm("John Doe", "john@example.com", "password123", null, null);
        Set<ConstraintViolation<AccountCreateForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("role", violations.iterator().next().getPropertyPath().toString());
    }
}
