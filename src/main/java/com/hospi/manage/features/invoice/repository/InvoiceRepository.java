package com.hospi.manage.features.invoice.repository;

import com.hospi.manage.features.invoice.entity.Invoice;
import com.hospi.manage.features.invoice.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Spring Data JPA repository for Invoice entities. */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByBookingId(Long bookingId);

    /** Find all invoices with the given status created within the date range. */
    List<Invoice> findByStatusAndCreatedAtBetween(InvoiceStatus status, LocalDateTime start, LocalDateTime end);

    /** Count invoices with the given status created within the date range. */
    long countByStatusAndCreatedAtBetween(InvoiceStatus status, LocalDateTime start, LocalDateTime end);

    /**
     * Find invoices with items eagerly fetched — avoids N+1 when iterating invoice items.
     */
    @Query("SELECT i FROM Invoice i JOIN FETCH i.items WHERE i.status = :status AND i.createdAt BETWEEN :start AND :end")
    List<Invoice> findWithItemsByStatusAndCreatedAtBetween(@Param("status") InvoiceStatus status,
                                                           @Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end);
}
