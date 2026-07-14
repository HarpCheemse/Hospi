package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.StayingGuest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link StayingGuest} entity — provides guest lookup by reservation.
 */
public interface StayingGuestRepository extends JpaRepository<StayingGuest, Long> {

    List<StayingGuest> findByReservationIdOrderByCreatedAtAsc(Long reservationId);

    long countByReservationId(Long reservationId);

    void deleteByReservationId(Long reservationId);
}
