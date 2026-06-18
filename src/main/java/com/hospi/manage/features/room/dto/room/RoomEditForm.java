package com.hospi.manage.features.room.dto.room;

import com.hospi.manage.features.room.enums.ConditionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RoomEditForm(
        @NotBlank(message = "Room number is required")
        @Pattern(regexp = "\\d+", message = "Room number must be numeric")
        String roomNumber,

        @NotNull
        Long roomTypeId,

        @NotNull
        ConditionStatus conditionStatus
) {
}
