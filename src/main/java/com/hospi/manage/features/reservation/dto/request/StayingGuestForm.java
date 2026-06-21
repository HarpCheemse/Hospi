package com.hospi.manage.features.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record StayingGuestForm(
    @NotBlank(message = "Guest name is required") String guestName,
    @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateOfBirth,
    String nationality
) {}
