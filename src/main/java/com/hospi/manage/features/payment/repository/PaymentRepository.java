package com.hospi.manage.features.payment.repository;

import com.hospi.manage.features.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Spring Data JPA repository for Payment entities. */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByReservationId(Long reservationId);

    Optional<Payment> findByReservationId(Long reservationId);

    boolean existsByReservationId(Long reservationId);

    /** Batch lookup payments for multiple reservation IDs. */
    List<Payment> findByReservationIdIn(List<Long> reservationIds);
}
