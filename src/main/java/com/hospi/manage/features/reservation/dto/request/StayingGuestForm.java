package com.hospi.manage.features.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Form for adding a staying guest to a reservation. */
public record StayingGuestForm(
    @NotBlank(message = "Guest name is required") @Size(max = 30, message = "Guest name must not exceed 30 characters") String guestName,
    @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateOfBirth,
    @Size(max = 30, message = "Nationality must not exceed 30 characters") String nationality
) {}
