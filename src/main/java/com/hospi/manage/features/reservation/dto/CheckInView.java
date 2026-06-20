package com.hospi.manage.features.reservation.dto;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.BookingSource;

import java.time.LocalDate;
import java.util.List;

public record CheckInView(
        Long id,
        String guestName,
        String guestEmail,
        String guestPhone,
        LocalDate checkInAt,
        LocalDate checkOutAt,
        BookingSource source,
        List<RoomDetailView> rooms
) {
    public static CheckInView from(Reservation reservation) {
        List<RoomDetailView> rooms = reservation.getDetails().stream()
                .map(d -> new RoomDetailView(
                        d.getRoomType().getName(),
                        d.getRoomCount()
                ))
                .toList();

        return new CheckInView(
                reservation.getId(),
                reservation.getGuestName(),
                reservation.getGuestEmail(),
                reservation.getGuestPhone(),
                reservation.getCheckInAt(),
                reservation.getCheckOutAt(),
                reservation.getSource(),
                rooms
        );
    }
}
