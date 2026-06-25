package com.hospi.manage.features.payment.repository;

import com.hospi.manage.features.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link Payment} entity — provides payment lookup by reservation.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReservationId(Long reservationId);

    boolean existsByReservationId(Long reservationId);
}
