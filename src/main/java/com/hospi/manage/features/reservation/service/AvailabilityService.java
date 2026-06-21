package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.room.dto.response.RoomInventory;
import com.hospi.manage.features.room.dto.response.RoomTypeAvailability;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AvailabilityService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    public List<RoomTypeAvailability> getAvailability(LocalDate checkIn,
                                                       LocalDate checkOut,
                                                       Long excludedReservationId) {
        Map<Long, Integer> booked = computeBookedCounts(checkIn,
                checkOut,
                excludedReservationId);
        List<RoomInventory> inventory = roomRepository.getRoomInventory();

        Map<Long, RoomType> typeMap = roomTypeRepository.findAll().stream()
                .collect(Collectors.toMap(RoomType::getId, rt -> rt));

        return inventory.stream()
                .map(i -> {
                    int bookedCount = booked.getOrDefault(i.roomTypeId(),
                            0);
                    RoomType roomType = typeMap.get(i.roomTypeId());
                    return new RoomTypeAvailability(
                            roomType,
                            i.totalRooms(),
                            i.totalRooms() - bookedCount
                    );
                })
                .toList();
    }

    public boolean canFulfil(Map<Long, Integer> required,
                              LocalDate checkIn,
                              LocalDate checkOut,
                              Long excludedReservationId) {
        Map<Long, Integer> available = getAvailability(checkIn,
                checkOut,
                excludedReservationId)
                .stream()
                .collect(Collectors.toMap(
                        a -> a.roomType().getId(),
                        RoomTypeAvailability::availableRooms
                ));

        return required.entrySet().stream()
                .allMatch(e -> available.getOrDefault(e.getKey(),
                        0) >= e.getValue());
    }

    public Map<Long, Integer> computeBookedCounts(LocalDate checkIn,
                                                   LocalDate checkOut,
                                                   Long excludedReservationId) {
        var reservations = excludedReservationId != null
                ? reservationRepository.findOverlappingExcluding(excludedReservationId,
                checkIn,
                checkOut)
                : reservationRepository.findOverlapping(checkIn,
                checkOut);

        return reservations.stream()
                .flatMap(r -> r.getDetails().stream())
                .collect(Collectors.groupingBy(
                        d -> d.getRoomType().getId(),
                        Collectors.summingInt(ReservationDetail::getRoomCount)
                ));
    }
}
