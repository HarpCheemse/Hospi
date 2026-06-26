package com.hospi.manage.features.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VerifyOtpFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenAllFieldsValid() {
        var form = new VerifyOtpForm("user@example.com", "123456");
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenEmailBlank() {
        var form = new VerifyOtpForm("", "123456");
        Set<ConstraintViolation<VerifyOtpForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenOtpBlank() {
        var form = new VerifyOtpForm("user@example.com", "");
        Set<ConstraintViolation<VerifyOtpForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("otp", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenOtpTooShort() {
        var form = new VerifyOtpForm("user@example.com", "12345");
        Set<ConstraintViolation<VerifyOtpForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("otp", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenOtpTooLong() {
        var form = new VerifyOtpForm("user@example.com", "1234567");
        Set<ConstraintViolation<VerifyOtpForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("otp", violations.iterator().next().getPropertyPath().toString());
    }
}
