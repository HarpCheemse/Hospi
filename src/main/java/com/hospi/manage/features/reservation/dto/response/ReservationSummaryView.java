package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Flat view model for a reservation summary used across manage, checkout, and receipt pages.
 * No JPA entities are exposed.
 */
public record ReservationSummaryView(
        Long id,
        String guestName,
        String guestEmail,
        String guestPhone,
        String guestNationality,
        LocalDate guestDateOfBirth,
        LocalDate checkInAt,
        LocalDate checkOutAt,
        ReservationStatus status,
        BookingSource source,
        String confirmationCode,
        BigDecimal totalPrice,
        LocalDateTime checkedOutAt,
        String checkedOutBy,
        BigDecimal lateCheckoutFeeApplied,
        BigDecimal extraGuestFeeApplied,
        List<ReservationDetailView> details
) {
    /** Create from a JPA entity. */
    public static ReservationSummaryView from(Reservation r) {
        return new ReservationSummaryView(
                r.getId(),
                r.getGuestName(),
                r.getGuestEmail(),
                r.getGuestPhone(),
                r.getGuestNationality(),
                r.getGuestDateOfBirth(),
                r.getCheckInAt(),
                r.getCheckOutAt(),
                r.getStatus(),
                r.getSource(),
                r.getConfirmationCode(),
                r.getTotalPrice(),
                r.getCheckedOutAt(),
                r.getCheckedOutBy(),
                r.getLateCheckoutFeeApplied(),
                r.getExtraGuestFeeApplied(),
                r.getDetails().stream().map(ReservationDetailView::from).toList()
        );
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkInAt, checkOutAt);
    }
}
