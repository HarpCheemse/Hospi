package com.hospi.manage.features.reservation.validation;

import com.hospi.manage.features.reservation.dto.request.DateSearchForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

@Component
public class DateSearchValidator {

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
        }
    }
}
