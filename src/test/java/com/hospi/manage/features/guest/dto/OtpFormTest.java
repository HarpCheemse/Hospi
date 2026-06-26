package com.hospi.manage.features.guest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OtpFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenOtpValid() {
        var form = new OtpForm("123456");
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenOtpBlank() {
        var form = new OtpForm("");
        Set<ConstraintViolation<OtpForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("otp", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenOtpNonNumeric() {
        var form = new OtpForm("abcdef");
        Set<ConstraintViolation<OtpForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("otp", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenOtpLessThanSixDigits() {
        var form = new OtpForm("12345");
        Set<ConstraintViolation<OtpForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("otp", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenOtpMoreThanSixDigits() {
        var form = new OtpForm("1234567");
        Set<ConstraintViolation<OtpForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("otp", violations.iterator().next().getPropertyPath().toString());
    }
}
