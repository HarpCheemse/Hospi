package com.hospi.manage.features.reservation.dto;

import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.reservation.entity.Reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record PaymentConfirmationView(
        Long id,
        String guestName,
        String guestEmail,
        LocalDate checkInAt,
        LocalDate checkOutAt,
        BigDecimal totalPrice,
        List<RoomDetailView> rooms,
        PaymentMethod[] paymentMethods
) {
    public static PaymentConfirmationView from(Reservation reservation) {
        List<RoomDetailView> rooms = reservation.getDetails().stream()
                .map(d -> new RoomDetailView(
                        d.getRoomType().getName(),
                        d.getRoomCount()
                ))
                .toList();

        return new PaymentConfirmationView(
                reservation.getId(),
                reservation.getGuestName(),
                reservation.getGuestEmail(),
                reservation.getCheckInAt(),
                reservation.getCheckOutAt(),
                reservation.getTotalPrice(),
                rooms,
                PaymentMethod.values()
        );
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkInAt, checkOutAt);
    }
}
