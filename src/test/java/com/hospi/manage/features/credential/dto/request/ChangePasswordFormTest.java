package com.hospi.manage.features.credential.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChangePasswordFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        var form = new ChangePasswordForm("currentPass", "newPass123", "newPass123");
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenCurrentPasswordBlank() {
        var form = new ChangePasswordForm("", "newPass123", "newPass123");
        Set<ConstraintViolation<ChangePasswordForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("currentPassword", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenNewPasswordBlank() {
        var form = new ChangePasswordForm("currentPass", "", "newPass123");
        Set<ConstraintViolation<ChangePasswordForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("newPassword", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenConfirmPasswordBlank() {
        var form = new ChangePasswordForm("currentPass", "newPass123", "");
        Set<ConstraintViolation<ChangePasswordForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("confirmPassword", violations.iterator().next().getPropertyPath().toString());
    }
}
