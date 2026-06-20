package com.hospi.manage.features.room.dto.response;

import com.hospi.manage.features.reservation.entity.StayingGuest;

import java.time.LocalDate;

public record GuestView(
        String name,
        LocalDate dateOfBirth,
        String nationality
) {
    public static GuestView from(StayingGuest guest) {
        return new GuestView(
                guest.getGuestName(),
                guest.getDateOfBirth(),
                guest.getNationality()
        );
    }
}
