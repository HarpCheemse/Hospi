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

    @Test
    void shouldPass_whenValid() {
        GuestDetailForm form = new GuestDetailForm();
        form.setGuestName("John Doe");
        form.setGuestEmail("john@example.com");
        form.setGuestPhone("+1234567890");
        form.setGuestDateOfBirth(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenGuestNameBlank() {
        GuestDetailForm form = new GuestDetailForm();
        form.setGuestName("");
        form.setGuestEmail("john@example.com");
        form.setGuestPhone("+1234567890");
        form.setGuestDateOfBirth(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestEmailBlank() {
        GuestDetailForm form = new GuestDetailForm();
        form.setGuestName("John Doe");
        form.setGuestEmail("");
        form.setGuestPhone("+1234567890");
        form.setGuestDateOfBirth(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestEmail", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestEmailInvalid() {
        GuestDetailForm form = new GuestDetailForm();
        form.setGuestName("John Doe");
        form.setGuestEmail("not-an-email");
        form.setGuestPhone("+1234567890");
        form.setGuestDateOfBirth(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestEmail", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestPhoneBlank() {
        GuestDetailForm form = new GuestDetailForm();
        form.setGuestName("John Doe");
        form.setGuestEmail("john@example.com");
        form.setGuestPhone("");
        form.setGuestDateOfBirth(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestPhone", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestDateOfBirthNull() {
        GuestDetailForm form = new GuestDetailForm();
        form.setGuestName("John Doe");
        form.setGuestEmail("john@example.com");
        form.setGuestPhone("+1234567890");

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestDateOfBirth", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestDateOfBirthFuture() {
        GuestDetailForm form = new GuestDetailForm();
        form.setGuestName("John Doe");
        form.setGuestEmail("john@example.com");
        form.setGuestPhone("+1234567890");
        form.setGuestDateOfBirth(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertEquals("guestDateOfBirth",
                violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldPass_whenNationalityNull() {
        GuestDetailForm form = new GuestDetailForm();
        form.setGuestName("John Doe");
        form.setGuestEmail("john@example.com");
        form.setGuestPhone("+1234567890");
        form.setGuestDateOfBirth(LocalDate.of(1990, 1, 1));
        form.setGuestNationality(null);

        Set<ConstraintViolation<GuestDetailForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }
}
