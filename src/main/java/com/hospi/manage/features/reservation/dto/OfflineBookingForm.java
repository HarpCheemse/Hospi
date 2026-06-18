package com.hospi.manage.features.reservation.dto;

import com.hospi.manage.features.room.dto.room.RoomSelection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;


public record OfflineBookingForm(

        @NotNull(message = "Check-in date is required") LocalDate checkInAt,

        @NotNull(message = "Check-out date is required") LocalDate checkOutAt,

        @NotBlank(message = "Guest name is required") String guestName,

        String guestEmail,

        @NotBlank(message = "Phone is required") String guestPhone,

        @NotNull(message = "Date of birth is required") LocalDate guestDateOfBirth,

        String guestNationality,

        List<RoomSelection> roomSelections

) {
}
