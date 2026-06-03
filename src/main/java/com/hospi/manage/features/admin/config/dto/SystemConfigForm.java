package com.hospi.manage.features.admin.config.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record SystemConfigForm(

        // Financial
        @NotNull(message = "Tax rate is required")
        @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
        @Digits(integer = 3, fraction = 2,
                message = "Tax rate must have up to 3 digits and 2 decimal places")
        BigDecimal taxRate,

        @NotNull(message = "Default deposit percentage is required")
        @DecimalMin(value = "0.00", message = "Default deposit percentage cannot be negative")
        @DecimalMax(value = "100.00", message = "Default deposit percentage cannot be larger than 100%")
        @Digits(integer = 3, fraction = 2,
                message = "Default deposit percentage must have up to 3 digits and 2 decimal places")
        BigDecimal defaultDepositPercentage,

        @NotNull(message = "Late checkout fee is required")
        @DecimalMin(value = "0.00", message = "Late checkout fee cannot be negative")
        @Digits(integer = 10, fraction = 2,
                message = "Late checkout fee must have up to 10 digits and 2 decimal places")
        BigDecimal lateCheckoutFee,

        @NotNull(message = "Extra guest fee is required")
        @DecimalMin(value = "0.00", message = "Extra guest fee cannot be negative")
        @Digits(integer = 10, fraction = 2,
                message = "Extra guest fee must have up to 10 digits and 2 decimal places")
        BigDecimal extraGuestFee,

        // Booking Rules
        @NotNull(message = "Pending booking expiry is required")
        @Min(value = 1, message = "Pending booking expiry must be at least 1 minute")
        Integer pendingBookingExpiryMinutes,

        @NotNull(message = "Cancellation hours before check-in is required")
        @Min(value = 0, message = "Cancellation hours cannot be negative")
        Integer cancellationHoursBeforeCheckin,

        @NotNull(message = "Maximum booking days is required")
        @Min(value = 1, message = "Maximum booking days must be at least 1")
        Integer maximumBookingDays,

        @NotNull(message = "Maximum rooms per booking is required")
        @Min(value = 1, message = "Maximum rooms per booking must be at least 1")
        Integer maximumRoomPerBook

) {
}