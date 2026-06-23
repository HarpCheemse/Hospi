package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByReservationId(Long reservationId);

    @EntityGraph(attributePaths = {"reservation"})
    List<Review> findByReservationIdIn(Collection<Long> reservationIds);

    boolean existsByReservationId(Long reservationId);
}
