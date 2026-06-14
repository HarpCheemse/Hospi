package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByStatusOrderByCheckInAtDesc(ReservationStatus status);

    List<Reservation> findByStatusInOrderByCheckInAtDesc(List<ReservationStatus> statuses);

    @Query("""
                select r from Reservation r
                where r.status <> 'CANCELLED'
                and r.checkInAt < :checkOut
                and r.checkOutAt > :checkIn
            """)
    List<Reservation> findOverlapping(LocalDate checkIn, LocalDate checkOut);

    Optional<Reservation> findByConfirmationCode(String confirmationCode);

    Optional<Reservation> findByGuestEmailAndConfirmationCode(String guestEmail, String confirmationCode);
}
