package com.hospi.manage.features.reservation.validation;

import com.hospi.manage.features.reservation.dto.OfflineBookingForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class OfflineBookingValidator {
    public void validateCreate(OfflineBookingForm form, BindingResult bindingResult) {
        validateRoomSelection(form,
                bindingResult);
    }

    public void validateRoomSelection(OfflineBookingForm form, BindingResult bindingResult) {

        if (form.roomSelections().isEmpty()) {
            bindingResult.reject(
                    "error.noRooms",
                    "At least one room type must be selected"
            );
        }
    }
}
