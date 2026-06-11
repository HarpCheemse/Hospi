package com.hospi.manage.features.reservation.service;

import com.hospi.manage.features.reservation.dto.CreateWalkInReservationForm;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.room.dto.RoomSelection;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomTypeRepository roomTypeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              RoomTypeRepository roomTypeRepository) {
        this.reservationRepository = reservationRepository;
        this.roomTypeRepository = roomTypeRepository;

    }


    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationRepository.findByStatusOrderByCheckInAtDesc(status);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    /// This function return RoomTypeId and amount of reservations for a given day range
    public Map<Long, Integer> getBookedCounts(
            LocalDate checkInAt,
            LocalDate checkOutAt
    ) {
        return reservationRepository.findOverlapping(
                        checkInAt,
                        checkOutAt
                )
                .stream()
                .flatMap(reservation -> reservation.getDetails().stream())
                .collect(Collectors.groupingBy(
                        detail -> detail.getRoomType().getId(),
                        Collectors.summingInt(
                                ReservationDetail::getRoomCount
                        )
                ));
    }

    @Transactional
    public Reservation createReservation(CreateWalkInReservationForm form) {
        Reservation reservation = new Reservation();
        reservation.setGuestName(form.guestName());
        reservation.setGuestEmail(form.guestEmail());
        reservation.setGuestPhone(form.guestPhone());
        reservation.setGuestDateOfBirth(form.guestDateOfBirth());
        reservation.setGuestNationality(form.guestNationality());
        reservation.setCheckInAt(form.checkInAt());
        reservation.setCheckOutAt(form.checkOutAt());
        reservation.setStatus(ReservationStatus.PENDING);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<ReservationDetail> details = new ArrayList<>();

        for (RoomSelection room : form.roomSelections()) {
            Long roomTypeId = room.roomTypeId();
            Integer count = room.roomCount();

            if (count == null || count <= 0) continue;

            RoomType roomType = roomTypeRepository.findById(roomTypeId).orElse(null);
            if (roomType == null) continue;

            ReservationDetail detail = new ReservationDetail();
            detail.setReservation(reservation);
            detail.setRoomType(roomType);
            detail.setRoomCount(count);
            detail.setBasePrice(roomType.getBasePrice());

            BigDecimal lineTotal = roomType.getBasePrice().multiply(BigDecimal.valueOf(count));
            detail.setTotalPrice(lineTotal);
            totalPrice = totalPrice.add(lineTotal);

            details.add(detail);
        }

        reservation.setDetails(details);
        reservation.setTotalPrice(totalPrice);

        return reservationRepository.save(reservation);
    }
}
