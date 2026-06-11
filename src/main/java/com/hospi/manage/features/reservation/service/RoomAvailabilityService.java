package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.room.dto.RoomInventory;
import com.hospi.manage.features.room.dto.RoomTypeAvailability;
import com.hospi.manage.features.room.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class RoomAvailabilityService {
    private final ReservationService reservationService;
    private final RoomRepository roomRepository;

    public RoomAvailabilityService(ReservationService reservationService, RoomRepository roomRepository) {
        this.reservationService = reservationService;
        this.roomRepository = roomRepository;
    }

    public List<RoomTypeAvailability> getAvailability(LocalDate checkInAt, LocalDate checkOutAt) {
        Map<Long, Integer> bookedCounts = reservationService.getBookedCounts(checkInAt,
                checkOutAt);
        List<RoomInventory> inventory = roomRepository.getRoomInventory();

        return inventory.stream().map(i -> {
            int booked = bookedCounts.getOrDefault(i.roomType().getId(),
                    0);
            return new RoomTypeAvailability(i.roomType(),
                    i.totalRooms(),
                    i.totalRooms() - booked);
        }).toList();
    }
}