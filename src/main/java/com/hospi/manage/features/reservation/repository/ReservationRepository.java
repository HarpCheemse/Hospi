package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for {@link Reservation} entity — provides query methods for filtering, searching, and
 * overlap detection.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByStatusOrderByCheckInAtDesc(ReservationStatus status);

    Page<Reservation> findByStatusOrderByCheckInAtDesc(ReservationStatus status, Pageable pageable);

    List<Reservation> findByStatusInOrderByCheckInAtDesc(List<ReservationStatus> statuses);

    List<Reservation> findByStatusAndCheckInAt(ReservationStatus status, LocalDate checkInAt);

    List<Reservation> findByStatusAndCheckOutAt(ReservationStatus status, LocalDate checkOutAt);

    /**
     * Find non-cancelled reservations whose stay periods overlap the given date range.
     */
    @Query("""
                select r from Reservation r
                where r.status <> 'CANCELLED'
                and r.checkInAt < :checkOut
                and r.checkOutAt > :checkIn
            """)
    List<Reservation> findOverlapping(LocalDate checkIn, LocalDate checkOut);

    /**
     * Return overlapping reservations excluding a given reservation id — used for check-out extension checks.
     */
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

    /**
     * Find checked-in reservations matching a guest name, phone, or booking code search.
     */
    @Query("""
                SELECT r FROM Reservation r
                WHERE r.status = 'CHECKED_IN'
                AND (LOWER(r.guestName) LIKE :search OR r.guestPhone LIKE :search OR LOWER(r.confirmationCode) LIKE :search)
                ORDER BY r.checkInAt DESC
            """)
    List<Reservation> findCheckedInFiltered(@Param("search") String search);

    /**
     * Find checked-in reservations matching a search term with pagination.
     */
    @Query(value = """
                SELECT r FROM Reservation r
                WHERE r.status = 'CHECKED_IN'
                AND (LOWER(r.guestName) LIKE :search OR r.guestPhone LIKE :search OR LOWER(r.confirmationCode) LIKE :search)
            """,
            countQuery = """
                SELECT COUNT(r) FROM Reservation r
                WHERE r.status = 'CHECKED_IN'
                AND (LOWER(r.guestName) LIKE :search OR r.guestPhone LIKE :search OR LOWER(r.confirmationCode) LIKE :search)
            """)
    Page<Reservation> findCheckedInFiltered(@Param("search") String search, Pageable pageable);

    /**
     * Find reservations matching the given statuses and guest name pattern.
     */
    @Query(value = """
                SELECT r FROM Reservation r
                WHERE r.status IN :statuses
                AND (LOWER(r.guestName) LIKE :guestName OR r.guestPhone LIKE :guestName OR LOWER(r.confirmationCode) LIKE :guestName)
                ORDER BY r.checkInAt DESC
            """)
    List<Reservation> findFiltered(@Param("statuses") List<ReservationStatus> statuses,
                                   @Param("guestName") String guestName);

    /**
     * Find reservations matching the given statuses and guest name with pagination, excluding online-pending.
     */
    @Query(value = """
                SELECT r FROM Reservation r
                WHERE r.status IN :statuses
                AND (LOWER(r.guestName) LIKE :guestName OR r.guestPhone LIKE :guestName OR LOWER(r.confirmationCode) LIKE :guestName)
                AND NOT (r.status = 'PENDING' AND r.source = 'ONLINE')
            """,
            countQuery = """
                SELECT COUNT(r) FROM Reservation r
                WHERE r.status IN :statuses
                AND (LOWER(r.guestName) LIKE :guestName OR r.guestPhone LIKE :guestName OR LOWER(r.confirmationCode) LIKE :guestName)
                AND NOT (r.status = 'PENDING' AND r.source = 'ONLINE')
            """)
    Page<Reservation> findFiltered(@Param("statuses") List<ReservationStatus> statuses,
                                   @Param("guestName") String guestName,
                                   Pageable pageable);

    /**
     * Find reservations matching statuses, guest name, and a date falling within the stay range.
     */
    @Query("""
                SELECT r FROM Reservation r
                WHERE r.status IN :statuses
                AND (LOWER(r.guestName) LIKE :guestName OR r.guestPhone LIKE :guestName OR LOWER(r.confirmationCode) LIKE :guestName)
                AND r.checkInAt <= :date
                AND r.checkOutAt >= :date
                ORDER BY r.checkInAt DESC
            """)
    List<Reservation> findFilteredWithDate(@Param("statuses") List<ReservationStatus> statuses,
                                           @Param("guestName") String guestName,
                                           @Param("date") LocalDate date);

    /**
     * Find reservations matching statuses, guest name, and a date with pagination, excluding online-pending.
     */
    @Query(value = """
                SELECT r FROM Reservation r
                WHERE r.status IN :statuses
                AND (LOWER(r.guestName) LIKE :guestName OR r.guestPhone LIKE :guestName OR LOWER(r.confirmationCode) LIKE :guestName)
                AND r.checkInAt <= :date
                AND r.checkOutAt >= :date
                AND NOT (r.status = 'PENDING' AND r.source = 'ONLINE')
            """,
            countQuery = """
                SELECT COUNT(r) FROM Reservation r
                WHERE r.status IN :statuses
                AND (LOWER(r.guestName) LIKE :guestName OR r.guestPhone LIKE :guestName OR LOWER(r.confirmationCode) LIKE :guestName)
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

    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime before);

    /** Batch lookup reservations by their IDs. */
    List<Reservation> findByIdIn(List<Long> ids);
}
