package com.hospi.manage.features.guest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Form for submitting a rating review for a reservation. */
public record ReviewForm(
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @NotNull(message = "Reservation is required")
        @Positive(message = "Invalid reservation")
        Long reservationId
) {
}
