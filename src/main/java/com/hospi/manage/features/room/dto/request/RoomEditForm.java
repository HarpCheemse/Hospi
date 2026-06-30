package com.hospi.manage.features.room.dto.request;

import com.hospi.manage.features.room.enums.ConditionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Form for editing a room's details. */
public record RoomEditForm(
        @NotBlank(message = "Room number is required")
        @Pattern(regexp = "\\d+", message = "Room number must be numeric")
        @Size(max = 10, message = "Room number must not exceed 10 characters")
        String roomNumber,

        @NotNull
        Long roomTypeId,

        @NotNull
        ConditionStatus conditionStatus
) {
}
