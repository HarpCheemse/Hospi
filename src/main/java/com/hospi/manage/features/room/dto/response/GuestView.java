package com.hospi.manage.features.room.dto.response;

import com.hospi.manage.features.reservation.entity.StayingGuest;

import java.time.LocalDate;

/** View model for displaying guest information. */
public record GuestView(
        String name,
        LocalDate dateOfBirth,
        String nationality
) {
    /** Create a guest view from a staying guest entity. */
    public static GuestView from(StayingGuest guest) {
        return new GuestView(
                guest.getGuestName(),
                guest.getDateOfBirth(),
                guest.getNationality()
        );
    }
}
