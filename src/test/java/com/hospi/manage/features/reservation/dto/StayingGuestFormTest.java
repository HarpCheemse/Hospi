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

class StayingGuestFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenValid() {
        StayingGuestForm form = new StayingGuestForm();
        form.setGuestName("Jane Guest");
        form.setDateOfBirth(LocalDate.of(1992, 6, 15));

        Set<ConstraintViolation<StayingGuestForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenGuestNameBlank() {
        StayingGuestForm form = new StayingGuestForm();
        form.setGuestName("");
        form.setDateOfBirth(LocalDate.of(1992, 6, 15));

        Set<ConstraintViolation<StayingGuestForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenDateOfBirthNull() {
        StayingGuestForm form = new StayingGuestForm();
        form.setGuestName("Jane Guest");

        Set<ConstraintViolation<StayingGuestForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("dateOfBirth", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenDateOfBirthFuture() {
        StayingGuestForm form = new StayingGuestForm();
        form.setGuestName("Jane Guest");
        form.setDateOfBirth(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<StayingGuestForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertEquals("dateOfBirth", violations.iterator().next().getPropertyPath().toString());
    }
}
