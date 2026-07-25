package com.hospi.manage.features.room.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Form for creating rooms on a floor for a room type. */
public record RoomCreateForm(
        @NotNull(message = "Floor is required")
        @Min(value = 1, message = "Floor can not be less than")
        Short floor,

        @NotNull(message = "Number of rooms is required")
        @Min(value = 1, message = "Must be at least 1")
        @Max(value = 99, message = "Must be at most 99")
        Integer numberOfRooms,

        @NotNull(message = "Room type is required")
        Long roomTypeId) {
}
