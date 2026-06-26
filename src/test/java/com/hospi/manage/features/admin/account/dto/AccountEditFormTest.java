package com.hospi.manage.features.admin.account.dto;

import com.hospi.manage.features.admin.account.enums.AccountStatus;
import com.hospi.manage.features.admin.account.enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AccountEditFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        var form = new AccountEditForm("John Doe", "john@example.com", "1234567890", Role.LEADER, AccountStatus.ACTIVE);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldPass_whenPhoneNull() {
        var form = new AccountEditForm("John Doe", "john@example.com", null, Role.LEADER, AccountStatus.ACTIVE);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenFullNameBlank() {
        var form = new AccountEditForm("", "john@example.com", null, Role.LEADER, AccountStatus.ACTIVE);
        Set<ConstraintViolation<AccountEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("fullName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenEmailBlank() {
        var form = new AccountEditForm("John Doe", "", null, Role.LEADER, AccountStatus.ACTIVE);
        Set<ConstraintViolation<AccountEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        var form = new AccountEditForm("John Doe", "not-an-email", null, Role.LEADER, AccountStatus.ACTIVE);
        Set<ConstraintViolation<AccountEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenRoleNull() {
        var form = new AccountEditForm("John Doe", "john@example.com", null, null, AccountStatus.ACTIVE);
        Set<ConstraintViolation<AccountEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("role", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenStatusNull() {
        var form = new AccountEditForm("John Doe", "john@example.com", null, Role.LEADER, null);
        Set<ConstraintViolation<AccountEditForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("status", violations.iterator().next().getPropertyPath().toString());
    }
}
