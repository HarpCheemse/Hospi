package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByStatusOrderByCheckInAtDesc(ReservationStatus status);

    Page<Reservation> findByStatusOrderByCheckInAtDesc(ReservationStatus status, Pageable pageable);

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

    List<Reservation> findByConfirmationCodeIn(Set<String> confirmationCodes);

    @Query("""
                SELECT r FROM Reservation r
                WHERE r.status = 'CHECKED_IN'
                AND (LOWER(r.guestName) LIKE :search OR r.guestPhone LIKE :search)
                ORDER BY r.checkInAt DESC
            """)
    List<Reservation> findCheckedInFiltered(@Param("search") String search);

    @Query(value = """
                SELECT r FROM Reservation r
                WHERE r.status = 'CHECKED_IN'
                AND (LOWER(r.guestName) LIKE :search OR r.guestPhone LIKE :search)
            """,
            countQuery = """
                SELECT COUNT(r) FROM Reservation r
                WHERE r.status = 'CHECKED_IN'
                AND (LOWER(r.guestName) LIKE :search OR r.guestPhone LIKE :search)
            """)
    Page<Reservation> findCheckedInFiltered(@Param("search") String search, Pageable pageable);

    @Query("""
                SELECT r FROM Reservation r
                WHERE r.status IN :statuses
                AND LOWER(r.guestName) LIKE :guestName
                ORDER BY r.checkInAt DESC
            """)
    List<Reservation> findFiltered(@Param("statuses") List<ReservationStatus> statuses,
                                   @Param("guestName") String guestName);

    @Query(value = """
                SELECT r FROM Reservation r
                WHERE r.status IN :statuses
                AND LOWER(r.guestName) LIKE :guestName
                AND NOT (r.status = 'PENDING' AND r.source = 'ONLINE')
            """,
            countQuery = """
                SELECT COUNT(r) FROM Reservation r
                WHERE r.status IN :statuses
                AND LOWER(r.guestName) LIKE :guestName
                AND NOT (r.status = 'PENDING' AND r.source = 'ONLINE')
            """)
    Page<Reservation> findFiltered(@Param("statuses") List<ReservationStatus> statuses,
                                   @Param("guestName") String guestName,
                                   Pageable pageable);

    @Query("""
                SELECT r FROM Reservation r
                WHERE r.status IN :statuses
                AND LOWER(r.guestName) LIKE :guestName
                AND r.checkInAt <= :date
                AND r.checkOutAt >= :date
                ORDER BY r.checkInAt DESC
            """)
    List<Reservation> findFilteredWithDate(@Param("statuses") List<ReservationStatus> statuses,
                                           @Param("guestName") String guestName,
                                           @Param("date") LocalDate date);

    @Query(value = """
                SELECT r FROM Reservation r
                WHERE r.status IN :statuses
                AND LOWER(r.guestName) LIKE :guestName
                AND r.checkInAt <= :date
                AND r.checkOutAt >= :date
                AND NOT (r.status = 'PENDING' AND r.source = 'ONLINE')
            """,
            countQuery = """
                SELECT COUNT(r) FROM Reservation r
                WHERE r.status IN :statuses
                AND LOWER(r.guestName) LIKE :guestName
                AND r.checkInAt <= :date
                AND r.checkOutAt >= :date
                AND NOT (r.status = 'PENDING' AND r.source = 'ONLINE')
            """)
    Page<Reservation> findFilteredWithDate(@Param("statuses") List<ReservationStatus> statuses,
                                           @Param("guestName") String guestName,
                                           @Param("date") LocalDate date,
                                           Pageable pageable);

    Optional<Reservation> findByGuestEmailAndConfirmationCode(String guestEmail, String confirmationCode);

    Optional<Reservation> findByPaymentIdempotencyKey(String paymentIdempotencyKey);
}
