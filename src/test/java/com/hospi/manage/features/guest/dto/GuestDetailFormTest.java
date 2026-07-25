package com.hospi.manage.features.guest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GuestDetailFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private static GuestDetailForm validForm() {
        return new GuestDetailForm("John Doe", "john@example.com", "+1234567890",
                LocalDate.of(1990, 1, 1), null, true);
    }

    @Test
    void shouldPass_whenValid() {
        GuestDetailForm form = validForm();

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenGuestNameBlank() {
        GuestDetailForm form = new GuestDetailForm("", "john@example.com", "+1234567890",
                LocalDate.of(1990, 1, 1), null, true);

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestEmailBlank() {
        GuestDetailForm form = new GuestDetailForm("John Doe", "", "+1234567890",
                LocalDate.of(1990, 1, 1), null, true);

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestEmail", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestEmailInvalid() {
        GuestDetailForm form = new GuestDetailForm("John Doe", "not-an-email", "+1234567890",
                LocalDate.of(1990, 1, 1), null, true);

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestEmail", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestPhoneBlank() {
        GuestDetailForm form = new GuestDetailForm("John Doe", "john@example.com", "",
                LocalDate.of(1990, 1, 1), null, true);

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestPhone", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestDateOfBirthNull() {
        GuestDetailForm form = new GuestDetailForm("John Doe", "john@example.com", "+1234567890",
                null, null, true);

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestDateOfBirth", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestDateOfBirthFuture() {
        GuestDetailForm form = new GuestDetailForm("John Doe", "john@example.com", "+1234567890",
                LocalDate.now().plusDays(1), null, true);

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertEquals("guestDateOfBirth",
                violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldPass_whenNationalityNull() {
        GuestDetailForm form = validForm();

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenTosNotAccepted() {
        GuestDetailForm form = new GuestDetailForm("John Doe", "john@example.com", "+1234567890",
                LocalDate.of(1990, 1, 1), null, false);

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertEquals("acceptedTos", violations.iterator().next().getPropertyPath().toString());
    }
}
