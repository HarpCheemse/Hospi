package com.hospi.manage.features.reservation.validation;

import com.hospi.manage.features.reservation.dto.request.OfflineBookingForm;
import com.hospi.manage.features.room.dto.response.RoomSelection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OfflineBookingFormValidatorTest {

    private final OfflineBookingFormValidator validator = new OfflineBookingFormValidator();

    private final LocalDate today = LocalDate.now();
    private final LocalDate futureCheckIn = today.plusDays(5);
    private final LocalDate futureCheckOut = today.plusDays(10);
    private final LocalDate pastDob = LocalDate.of(1990, 1, 1);

    @Test
    void validateCreate_shouldPass_whenValid() {
        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", "john@example.com", "+1234567890",
                pastDob, "US",
                List.of(new RoomSelection(1L, 2))
        );

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void validateCreate_shouldReject_whenCheckInPast() {
        OfflineBookingForm form = new OfflineBookingForm(
                today.minusDays(1), futureCheckOut,
                "John Doe", null, "+1234567890",
                pastDob, null,
                List.of(new RoomSelection(1L, 2))
        );

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("checkInAt"));
        assertEquals("Check-in date must be today or later",
                errors.getFieldError("checkInAt").getDefaultMessage());
    }

    @Test
    void validateCreate_shouldReject_whenCheckOutBeforeCheckIn() {
        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckIn.minusDays(1),
                "John Doe", null, "+1234567890",
                pastDob, null,
                List.of(new RoomSelection(1L, 2))
        );

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("checkOutAt"));
        assertEquals("Check-out must be after check-in",
                errors.getFieldError("checkOutAt").getDefaultMessage());
    }

    @Test
    void validateCreate_shouldReject_whenDateOfBirthFuture() {
        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", null, "+1234567890",
                today.plusDays(1), null,
                List.of(new RoomSelection(1L, 2))
        );

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("guestDateOfBirth"));
        assertEquals("Date of birth must be in the past",
                errors.getFieldError("guestDateOfBirth").getDefaultMessage());
    }

    @Test
    void validateCreate_shouldReject_whenRoomSelectionsEmpty() {
        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", null, "+1234567890",
                pastDob, null,
                List.of()
        );

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasGlobalErrors());
        assertEquals("At least one room type must be selected",
                errors.getGlobalError().getDefaultMessage());
    }

    @Test
    void validateCreate_shouldReject_whenAllRoomCountsZero() {
        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", null, "+1234567890",
                pastDob, null,
                List.of(new RoomSelection(1L, 0), new RoomSelection(2L, 0))
        );

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasGlobalErrors());
        assertEquals("At least one room type must have a count greater than 0",
                errors.getGlobalError().getDefaultMessage());
    }

    @Test
    void validateCreate_shouldReject_whenRoomSelectionsNull() {
        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", null, "+1234567890",
                pastDob, null,
                null
        );

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasGlobalErrors());
        assertEquals("At least one room type must be selected",
                errors.getGlobalError().getDefaultMessage());
    }
}
