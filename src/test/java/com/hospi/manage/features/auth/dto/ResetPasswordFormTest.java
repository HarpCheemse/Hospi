package com.hospi.manage.features.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenPasswordsNull() {
        var form = new ResetPasswordForm(null, null);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldPass_whenPasswordsValidLength() {
        var form = new ResetPasswordForm("newpassword", "confirmpassword");
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenNewPasswordTooLong() {
        var form = new ResetPasswordForm("a".repeat(256), "confirmpassword");
        Set<ConstraintViolation<ResetPasswordForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("newPassword", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenConfirmPasswordTooLong() {
        var form = new ResetPasswordForm("newpassword", "a".repeat(256));
        Set<ConstraintViolation<ResetPasswordForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("confirmPassword", violations.iterator().next().getPropertyPath().toString());
    }
}
