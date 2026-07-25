package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.StayingGuest;

import java.time.LocalDate;

/**
 * View model for a staying guest record.
 * No JPA entities are exposed.
 */
public record StayingGuestView(
        Long id,
        String guestName,
        LocalDate dateOfBirth,
        String nationality
) {
    /** Create from a JPA entity. */
    public static StayingGuestView from(StayingGuest guest) {
        return new StayingGuestView(
                guest.getId(),
                guest.getGuestName(),
                guest.getDateOfBirth(),
                guest.getNationality()
        );
    }
}
