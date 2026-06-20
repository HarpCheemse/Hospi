package com.hospi.manage.features.guest.validation;

import com.hospi.manage.features.reservation.dto.DateSearchForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingDateValidatorTest {

    private final BookingDateValidator validator = new BookingDateValidator();

    @Test
    void validate_shouldPass_whenCheckInTodayAndCheckOutAfter() {
        DateSearchForm form = new DateSearchForm();
        form.setCheckInAt(LocalDate.now());
        form.setCheckOutAt(LocalDate.now().plusDays(1));

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void validate_shouldPass_whenCheckInFutureAndCheckOutAfter() {
        DateSearchForm form = new DateSearchForm();
        form.setCheckInAt(LocalDate.now().plusDays(5));
        form.setCheckOutAt(LocalDate.now().plusDays(10));

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void validate_shouldReject_whenCheckInPast() {
        DateSearchForm form = new DateSearchForm();
        form.setCheckInAt(LocalDate.now().minusDays(1));
        form.setCheckOutAt(LocalDate.now().plusDays(5));

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("checkInAt"));
        assertEquals("Check-in date must be today or later",
                errors.getFieldError("checkInAt").getDefaultMessage());
    }

    @Test
    void validate_shouldReject_whenCheckOutSameAsCheckIn() {
        DateSearchForm form = new DateSearchForm();
        form.setCheckInAt(LocalDate.now());
        form.setCheckOutAt(LocalDate.now());

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("checkOutAt"));
        assertEquals("Check-out must be after check-in",
                errors.getFieldError("checkOutAt").getDefaultMessage());
    }

    @Test
    void validate_shouldReject_whenCheckOutBeforeCheckIn() {
        DateSearchForm form = new DateSearchForm();
        form.setCheckInAt(LocalDate.now().plusDays(5));
        form.setCheckOutAt(LocalDate.now().plusDays(3));

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("checkOutAt"));
    }
}
