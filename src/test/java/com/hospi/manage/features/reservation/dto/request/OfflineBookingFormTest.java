package com.hospi.manage.features.reservation.dto.request;

import com.hospi.manage.features.room.dto.response.RoomSelection;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OfflineBookingFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPass_whenValid() {
        OfflineBookingForm form = new OfflineBookingForm(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "John Doe",
                "john@example.com",
                "+1234567890",
                LocalDate.of(1990, 1, 1),
                "US",
                List.of(new RoomSelection(1L, 2))
        );

        Set<ConstraintViolation<OfflineBookingForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenCheckInAtNull() {
        OfflineBookingForm form = new OfflineBookingForm(
                null,
                LocalDate.now().plusDays(10),
                "John Doe", null, "+1234567890",
                LocalDate.of(1990, 1, 1), null,
                List.of()
        );

        Set<ConstraintViolation<OfflineBookingForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("checkInAt", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenCheckOutAtNull() {
        OfflineBookingForm form = new OfflineBookingForm(
                LocalDate.now().plusDays(5), null,
                "John Doe", null, "+1234567890",
                LocalDate.of(1990, 1, 1), null,
                List.of()
        );

        Set<ConstraintViolation<OfflineBookingForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("checkOutAt", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestNameBlank() {
        OfflineBookingForm form = new OfflineBookingForm(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "", null, "+1234567890",
                LocalDate.of(1990, 1, 1), null,
                List.of()
        );

        Set<ConstraintViolation<OfflineBookingForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestName", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestPhoneBlank() {
        OfflineBookingForm form = new OfflineBookingForm(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "John Doe", null, "",
                LocalDate.of(1990, 1, 1), null,
                List.of()
        );

        Set<ConstraintViolation<OfflineBookingForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestPhone", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestDateOfBirthNull() {
        OfflineBookingForm form = new OfflineBookingForm(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "John Doe", null, "+1234567890",
                null, null,
                List.of()
        );

        Set<ConstraintViolation<OfflineBookingForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestDateOfBirth", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFail_whenGuestEmailInvalid() {
        OfflineBookingForm form = new OfflineBookingForm(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "John Doe",
                "not-an-email",
                "+1234567890",
                LocalDate.of(1990, 1, 1), null,
                List.of()
        );

        Set<ConstraintViolation<OfflineBookingForm>> violations = validator.validate(form);

        assertEquals(1, violations.size());
        assertEquals("guestEmail", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldPass_whenGuestEmailNull() {
        OfflineBookingForm form = new OfflineBookingForm(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "John Doe", null, "+1234567890",
                LocalDate.of(1990, 1, 1), null,
                List.of()
        );

        Set<ConstraintViolation<OfflineBookingForm>> violations = validator.validate(form);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFail_whenGuestDateOfBirthFuture() {
        OfflineBookingForm form = new OfflineBookingForm(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                "John Doe", null, "+1234567890",
                LocalDate.now().plusDays(1), null,
                List.of()
        );

        Set<ConstraintViolation<OfflineBookingForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertEquals("guestDateOfBirth",
                violations.iterator().next().getPropertyPath().toString());
    }
}
