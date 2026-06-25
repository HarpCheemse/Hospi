package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Review} entity — provides review lookup by reservation.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByReservationId(Long reservationId);

    /**
     * Find all reviews for the given reservation IDs, eagerly loading the associated reservation.
     */
    @EntityGraph(attributePaths = {"reservation"})
    List<Review> findByReservationIdIn(Collection<Long> reservationIds);

    boolean existsByReservationId(Long reservationId);
}
