package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
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

    /** Average rating across all reviews. */
    @Query("SELECT AVG(r.rating) FROM Review r")
    Optional<BigDecimal> findAverageRating();

    /** Total number of reviews. */
    @Query("SELECT COUNT(r) FROM Review r")
    int countReviews();
}
