package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.ReservationDetail;

import java.math.BigDecimal;

/**
 * View model for a single line item on a reservation (room type + quantity + pricing).
 * No JPA entities are exposed.
 */
public record ReservationDetailView(
        Long roomTypeId,
        String roomTypeName,
        Integer roomCount,
        BigDecimal basePrice,
        BigDecimal totalPrice
) {
    /** Create from a JPA entity. */
    public static ReservationDetailView from(ReservationDetail detail) {
        return new ReservationDetailView(
                detail.getRoomType().getId(),
                detail.getRoomType().getName(),
                detail.getRoomCount(),
                detail.getBasePrice(),
                detail.getTotalPrice()
        );
    }
}
