package com.hospi.manage.features.guest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookingTrackFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenValid() {
        BookingTrackForm form = new BookingTrackForm();
        form.setEmail("john@example.com");
        form.setBookingCode("HSP-ABC123");

        Set<ConstraintViolation<BookingTrackForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenEmailBlank() {
        BookingTrackForm form = new BookingTrackForm();
        form.setEmail("");
        form.setBookingCode("HSP-ABC123");

        Set<ConstraintViolation<BookingTrackForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenEmailInvalid() {
        BookingTrackForm form = new BookingTrackForm();
        form.setEmail("not-an-email");
        form.setBookingCode("HSP-ABC123");

        Set<ConstraintViolation<BookingTrackForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenBookingCodeBlank() {
        BookingTrackForm form = new BookingTrackForm();
        form.setEmail("john@example.com");
        form.setBookingCode("");

        Set<ConstraintViolation<BookingTrackForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("bookingCode", violations.iterator().next().getPropertyPath().toString());
    }
}
