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

    //This exlude the reservation for room iventory. this is used for extends check out time
    @Query("""
                SELECT r FROM Reservation r
                WHERE r.status IN ('CONFIRMED', 'CHECKED_IN')
                AND r.id <> :excludedId
                AND r.checkInAt < :end
                AND r.checkOutAt > :start
            """)
    List<Reservation> findOverlappingExcluding(Long excludedId,
                                               LocalDate start,
                                               LocalDate end);

    Optional<Reservation> findByConfirmationCode(String confirmationCode);

    Optional<Reservation> findByGuestEmailAndConfirmationCode(String guestEmail, String confirmationCode);
}
