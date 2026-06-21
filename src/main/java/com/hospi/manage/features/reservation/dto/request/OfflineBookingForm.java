package com.hospi.manage.features.reservation.dto.request;

import com.hospi.manage.features.room.dto.response.RoomSelection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;


public record OfflineBookingForm(

        @NotNull(message = "Check-in date is required") LocalDate checkInAt,

        @NotNull(message = "Check-out date is required") LocalDate checkOutAt,

        @NotBlank(message = "Guest name is required") @Size(max = 30, message = "Guest name must not exceed 30 characters") String guestName,

        @Email(message = "Invalid email format") @Size(max = 255, message = "Email must not exceed 255 characters") String guestEmail,

        @NotBlank(message = "Phone is required") @Size(max = 20, message = "Phone must not exceed 20 characters") String guestPhone,

        @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past") LocalDate guestDateOfBirth,

        @Size(max = 30, message = "Nationality must not exceed 30 characters") String guestNationality,

        List<RoomSelection> roomSelections

) {
}
