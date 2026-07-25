package com.hospi.manage.features.guest.validation;

import com.hospi.manage.features.config.entity.SystemConfig;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.reservation.dto.request.DateSearchForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingDateValidatorTest {

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private BookingDateValidator validator;

    private SystemConfig configWithMaxDays(int max) {
        var config = new SystemConfig();
        config.setMaximumBookingDays(max);
        return config;
    }

    @Test
    void validate_shouldPass_whenCheckInTodayAndCheckOutAfter() {
        when(systemConfigService.getConfig()).thenReturn(configWithMaxDays(30));
        DateSearchForm form = new DateSearchForm(LocalDate.now(), LocalDate.now().plusDays(1));

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void validate_shouldPass_whenCheckInFutureAndCheckOutAfter() {
        when(systemConfigService.getConfig()).thenReturn(configWithMaxDays(30));
        DateSearchForm form = new DateSearchForm(LocalDate.now().plusDays(5), LocalDate.now().plusDays(10));

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    void validate_shouldReject_whenCheckInPast() {
        when(systemConfigService.getConfig()).thenReturn(configWithMaxDays(30));
        DateSearchForm form = new DateSearchForm(LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("checkInAt"));
        assertEquals("Check-in date must be today or later",
                errors.getFieldError("checkInAt").getDefaultMessage());
    }

    @Test
    void validate_shouldReject_whenCheckOutSameAsCheckIn() {
        DateSearchForm form = new DateSearchForm(LocalDate.now(), LocalDate.now());

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("checkOutAt"));
        assertEquals("Check-out must be after check-in",
                errors.getFieldError("checkOutAt").getDefaultMessage());
    }

    @Test
    void validate_shouldReject_whenCheckOutBeforeCheckIn() {
        DateSearchForm form = new DateSearchForm(LocalDate.now().plusDays(5), LocalDate.now().plusDays(3));

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("checkOutAt"));
    }

    @Test
    void validate_shouldReject_whenNightsExceedMaxDays() {
        when(systemConfigService.getConfig()).thenReturn(configWithMaxDays(5));
        DateSearchForm form = new DateSearchForm(LocalDate.now(), LocalDate.now().plusDays(10));

        BindingResult errors = new BeanPropertyBindingResult(form, "form");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("checkOutAt"));
        assertTrue(errors.getFieldError("checkOutAt").getDefaultMessage().contains("5"));
    }
}
