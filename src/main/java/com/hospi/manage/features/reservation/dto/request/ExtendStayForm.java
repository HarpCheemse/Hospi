package com.hospi.manage.features.reservation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExtendStayForm(
        @NotNull(message = "Number of extra nights is required")
        @Min(value = 1, message = "Extra nights must be at least 1")
        Integer extraDays
) {
}
