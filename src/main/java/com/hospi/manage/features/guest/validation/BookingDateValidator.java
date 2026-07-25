package com.hospi.manage.features.guest.validation;

import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.reservation.dto.request.DateSearchForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Validator for the guest-facing date-search form ({@link DateSearchForm}). */
@Component
@RequiredArgsConstructor
public class BookingDateValidator {

    private final SystemConfigService systemConfigService;

    /**
     * Validate check-in and check-out dates.
     *
     * @param form          the date search form data
     * @param bindingResult binding result to populate with validation failures
     */
    public void validate(DateSearchForm form, BindingResult bindingResult) {
        validateCheckInAt(form, bindingResult);
        validateCheckOutAt(form, bindingResult);
    }

    private void validateCheckInAt(DateSearchForm form, BindingResult bindingResult) {
        if (form.checkInAt() != null && form.checkInAt().isBefore(LocalDate.now())) {
            bindingResult.rejectValue("checkInAt",
                    "error.pastCheckIn",
                    "Check-in date must be today or later");
        }
    }

    private void validateCheckOutAt(DateSearchForm form, BindingResult bindingResult) {
        if (form.checkOutAt() != null && form.checkInAt() != null
                && !form.checkOutAt().isAfter(form.checkInAt())) {
            bindingResult.rejectValue("checkOutAt",
                    "error.checkOutBeforeCheckIn",
                    "Check-out must be after check-in");
            return;
        }

        if (form.checkInAt() != null && form.checkOutAt() != null) {
            long nights = ChronoUnit.DAYS.between(form.checkInAt(), form.checkOutAt());
            var config = systemConfigService.getConfig();
            if (config.getMaximumBookingDays() != null && nights > config.getMaximumBookingDays()) {
                bindingResult.rejectValue("checkOutAt",
                        "error.maxBookingDays",
                        "Booking duration cannot exceed " + config.getMaximumBookingDays() + " nights");
            }
        }
    }
}
