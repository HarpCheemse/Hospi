package com.hospi.manage.features.reservation.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExtendStayFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenExtraDaysValid() {
        var form = new ExtendStayForm(1);
        assertTrue(validator.validate(form).isEmpty());
    }

    @Test
    void shouldFail_whenExtraDaysNull() {
        var form = new ExtendStayForm(null);
        Set<ConstraintViolation<ExtendStayForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("extraDays", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenExtraDaysBelowOne() {
        var form = new ExtendStayForm(0);
        Set<ConstraintViolation<ExtendStayForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertEquals("extraDays", violations.iterator().next().getPropertyPath().toString());
    }
}
