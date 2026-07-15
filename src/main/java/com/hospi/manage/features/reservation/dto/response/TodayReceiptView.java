package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Summary view for today's receipts list. No JPA entities exposed. */
public record TodayReceiptView(
        Long id,
        String guestName,
        String roomNumbers,
        BigDecimal totalCharges,
        BigDecimal amountPaid,
        LocalDateTime checkedOutAt,
        String checkedOutBy
) {
    public static TodayReceiptView from(Reservation r, BigDecimal totalCharges, BigDecimal amountPaid) {
        String rooms = r.getDetails() != null
                ? r.getDetails().stream()
                        .map(d -> d.getRoomType().getName() + " x" + d.getRoomCount())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("")
                : "";
        return new TodayReceiptView(
                r.getId(), r.getGuestName(), rooms,
                totalCharges, amountPaid,
                r.getCheckedOutAt(), r.getCheckedOutBy());
    }
}
