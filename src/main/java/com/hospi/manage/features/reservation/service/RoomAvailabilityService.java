package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.room.dto.RoomTypeAvailability;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomAvailabilityService {

    private final AvailabilityEngine engine;

    public RoomAvailabilityService(AvailabilityEngine engine) {
        this.engine = engine;
    }

    /**
     * For search and new bookings — includes all reservations.
     */
    public List<RoomTypeAvailability> getAvailability(LocalDate checkIn, LocalDate checkOut) {
        return engine.getAvailability(checkIn,
                checkOut,
                null);
    }

    /**
     * For extensions and edits — excludes one reservation.
     */
    public List<RoomTypeAvailability> getAvailabilityExcluding(Long excludedId,
                                                               LocalDate checkIn,
                                                               LocalDate checkOut) {
        return engine.getAvailability(checkIn,
                checkOut,
                excludedId);
    }
}