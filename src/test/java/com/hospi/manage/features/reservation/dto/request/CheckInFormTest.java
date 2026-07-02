package com.hospi.manage.features.reservation.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CheckInFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenBookingCodeNull() {
        var form = new CheckInForm(null);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldPass_whenBookingCodeValidLength() {
        var form = new CheckInForm("ABC123");
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldPass_whenBookingCodeAtMaxLength() {
        var form = new CheckInForm("1234567890");
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenBookingCodeTooLong() {
        var form = new CheckInForm("12345678901");
        Set<ConstraintViolation<CheckInForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("bookingCode", violations.iterator().next().getPropertyPath().toString());
    }
}
