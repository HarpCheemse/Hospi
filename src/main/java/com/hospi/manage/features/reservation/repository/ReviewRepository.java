package com.hospi.manage.features.reservation.repository;

import com.hospi.manage.features.reservation.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByReservationId(Long reservationId);

    boolean existsByReservationId(Long reservationId);
}
