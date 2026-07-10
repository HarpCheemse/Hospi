package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.reservation.repository.RoomAssignmentRepository;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Service for room swaps — change one unit of a room type to a higher-tier type. */
@Service
@RequiredArgsConstructor
public class RoomUpgradeService {

    private final ReservationRepository reservationRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final RoomAssignmentRepository roomAssignmentRepository;

    /** Swap one unit from currentRoomType to newRoomType. Splits the detail if roomCount > 1. */
    @Transactional
    public void swapOne(Long reservationId, Long currentRoomTypeId, Long newRoomTypeId) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED
                && reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("Only confirmed or checked-in reservations can be swapped");
        }

        var newRoomType = roomTypeRepository.findById(newRoomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Room type"));

        var detail = reservation.getDetails().stream()
                .filter(d -> d.getRoomType().getId().equals(currentRoomTypeId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Room type not found on reservation"));

        var oldBasePrice = detail.getBasePrice();
        if (newRoomType.getBasePrice().compareTo(oldBasePrice) <= 0) {
            throw new IllegalStateException("New room type must have a higher price than the current one");
        }
        if (reservation.getCheckInAt() == null || reservation.getCheckOutAt() == null) {
            throw new IllegalStateException("Reservation has no dates");
        }

        long remainingNights = reservation.getStatus() == ReservationStatus.CHECKED_IN
                ? ChronoUnit.DAYS.between(LocalDate.now(), reservation.getCheckOutAt())
                : ChronoUnit.DAYS.between(reservation.getCheckInAt(), reservation.getCheckOutAt());

        if (remainingNights <= 0) {
            throw new IllegalStateException("No remaining nights for swap");
        }

        var delta = newRoomType.getBasePrice().subtract(oldBasePrice)
                .multiply(BigDecimal.valueOf(remainingNights));

        if (detail.getRoomCount() > 1) {
            detail.setRoomCount(detail.getRoomCount() - 1);
            detail.setTotalPrice(detail.getBasePrice()
                    .multiply(BigDecimal.valueOf(detail.getRoomCount()))
                    .multiply(BigDecimal.valueOf(remainingNights)));

            var newDetail = new ReservationDetail();
            newDetail.setReservation(reservation);
            newDetail.setRoomType(newRoomType);
            newDetail.setRoomCount(1);
            newDetail.setBasePrice(newRoomType.getBasePrice());
            newDetail.setTotalPrice(newRoomType.getBasePrice().multiply(BigDecimal.valueOf(remainingNights)));
            reservation.getDetails().add(newDetail);
        } else {
            detail.setRoomType(newRoomType);
            detail.setBasePrice(newRoomType.getBasePrice());
            detail.setTotalPrice(detail.getBasePrice().multiply(BigDecimal.valueOf(remainingNights)));
        }

        reservation.setTotalPrice(reservation.getTotalPrice().add(delta));

        if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
            var oldAssignments = roomAssignmentRepository
                    .findByReservationIdOrderByAssignedAtAsc(reservationId);
            for (var a : oldAssignments) {
                if (a.getRoom().getRoomType().getId().equals(currentRoomTypeId)) {
                    var room = a.getRoom();
                    room.setOccupancyStatus(OccupancyStatus.VACANT);
                    roomRepository.save(room);
                    roomAssignmentRepository.delete(a);
                }
            }

            var vacantRooms = roomRepository
                    .findByRoomTypeIdAndOccupancyStatusAndActiveTrue(newRoomTypeId, OccupancyStatus.VACANT);
            if (vacantRooms.isEmpty()) {
                throw new IllegalStateException("No vacant rooms available of type " + newRoomType.getName());
            }
            var newRoom = vacantRooms.get(0);
            newRoom.setOccupancyStatus(OccupancyStatus.OCCUPIED);
            roomRepository.save(newRoom);

            var assignment = new RoomAssignment();
            assignment.setReservation(reservation);
            assignment.setRoom(newRoom);
            roomAssignmentRepository.save(assignment);
        }

        reservationRepository.save(reservation);
    }
}
