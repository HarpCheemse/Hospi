package com.hospi.manage.features.reservation.validator;

import com.hospi.manage.features.reservation.dto.CreateWalkInReservationForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class WalkInReservationValidator {
    public void validateCreate(CreateWalkInReservationForm form, BindingResult bindingResult) {
        validateRoomSelection(form,
                bindingResult);
    }

    public void validateRoomSelection(CreateWalkInReservationForm form, BindingResult bindingResult) {

        if (form.roomSelections().isEmpty()) {
            bindingResult.reject(
                    "error.noRooms",
                    "At least one room type must be selected"
            );
        }
    }
}