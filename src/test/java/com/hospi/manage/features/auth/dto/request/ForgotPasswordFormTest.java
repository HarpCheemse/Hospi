package com.hospi.manage.features.auth.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ForgotPasswordFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenEmailValid() {
        var form = new ForgotPasswordForm("user@example.com");
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenEmailBlank() {
        var form = new ForgotPasswordForm("");
        Set<ConstraintViolation<ForgotPasswordForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        var form = new ForgotPasswordForm("not-an-email");
        Set<ConstraintViolation<ForgotPasswordForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }
}
