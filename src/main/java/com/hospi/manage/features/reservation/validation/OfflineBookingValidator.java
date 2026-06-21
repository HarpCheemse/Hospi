package com.hospi.manage.features.reservation.validation;

import com.hospi.manage.features.reservation.dto.OfflineBookingForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

@Component
public class OfflineBookingValidator {

    public void validate(OfflineBookingForm form, BindingResult bindingResult) {
        validateCheckInAt(form, bindingResult);
        validateCheckOutAt(form, bindingResult);
        validateDateOfBirth(form, bindingResult);
        validateRoomSelection(form, bindingResult);
    }

    private void validateCheckInAt(OfflineBookingForm form, BindingResult bindingResult) {
        if (form.checkInAt() != null && form.checkInAt().isBefore(LocalDate.now())) {
            bindingResult.rejectValue("checkInAt",
                    "error.pastCheckIn",
                    "Check-in date must be today or later");
        }
    }

    private void validateCheckOutAt(OfflineBookingForm form, BindingResult bindingResult) {
        if (form.checkOutAt() != null && form.checkInAt() != null
                && !form.checkOutAt().isAfter(form.checkInAt())) {
            bindingResult.rejectValue("checkOutAt",
                    "error.checkOutBeforeCheckIn",
                    "Check-out must be after check-in");
        }
    }

    private void validateDateOfBirth(OfflineBookingForm form, BindingResult bindingResult) {
        if (form.guestDateOfBirth() != null && !form.guestDateOfBirth().isBefore(LocalDate.now())) {
            bindingResult.rejectValue("guestDateOfBirth",
                    "error.futureDateOfBirth",
                    "Date of birth must be in the past");
        }
    }

    private void validateRoomSelection(OfflineBookingForm form, BindingResult bindingResult) {
        if (form.roomSelections() == null || form.roomSelections().isEmpty()) {
            bindingResult.reject(
                    "error.noRooms",
                    "At least one room type must be selected");
            return;
        }

        boolean hasValidSelection = form.roomSelections().stream()
                .anyMatch(rs -> rs.roomCount() != null && rs.roomCount() > 0);

        if (!hasValidSelection) {
            bindingResult.reject(
                    "error.noRoomCount",
                    "At least one room type must have a count greater than 0");
        }
    }
}
