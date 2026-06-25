package com.hospi.manage.features.reservation.dto.request;

import jakarta.validation.constraints.NotNull;

/** Form for assigning a room to a reservation. */
public record AssignRoomForm(
        @NotNull(message = "Please select a room to assign")
        Long roomId
) {
}
