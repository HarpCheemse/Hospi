package com.hospi.manage.features.reservation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DateSearchFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenValid() {
        DateSearchForm form = new DateSearchForm();
        form.setCheckInAt(LocalDate.now());
        form.setCheckOutAt(LocalDate.now().plusDays(3));

        Set<ConstraintViolation<DateSearchForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenCheckInAtNull() {
        DateSearchForm form = new DateSearchForm();
        form.setCheckOutAt(LocalDate.now().plusDays(3));

        Set<ConstraintViolation<DateSearchForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("checkInAt", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenCheckOutAtNull() {
        DateSearchForm form = new DateSearchForm();
        form.setCheckInAt(LocalDate.now());

        Set<ConstraintViolation<DateSearchForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("checkOutAt", violations.iterator().next().getPropertyPath().toString());
    }
}
