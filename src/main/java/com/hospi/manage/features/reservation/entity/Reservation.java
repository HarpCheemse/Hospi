package com.hospi.manage.features.reservation.entity;

import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.CancellationReason;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a hotel booking with guest details, dates, status, and associated line items.
 */
@Entity
@Table(
    name = "reservations",
    indexes = {
        @Index(name = "idx_reservation_status_created_at", columnList = "status, created_at"),
        @Index(name = "idx_reservation_status_checkin", columnList = "status, check_in_at")
    }
)
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "hotel_id")
    private Long hotelId;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "guest_date_of_birth")
    private LocalDate guestDateOfBirth;

    @Column(name = "guest_nationality")
    private String guestNationality;

    @Column(name = "check_in_at", nullable = false)
    private LocalDate checkInAt;

    @Column(name = "check_out_at", nullable = false)
    private LocalDate checkOutAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private BookingSource source;

    @Column(name = "confirmation_code", length = 50)
    private String confirmationCode;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "payment_idempotency_key", length = 100)
    private String paymentIdempotencyKey;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "checked_in_by")
    private String checkedInBy;

    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;

    @Column(name = "checked_out_by")
    private String checkedOutBy;

    @Column(name = "late_checkout_fee_applied", precision = 10, scale = 2)
    private BigDecimal lateCheckoutFeeApplied;

    @Column(name = "extra_guest_fee_applied", precision = 10, scale = 2)
    private BigDecimal extraGuestFeeApplied;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationDetail> details = new ArrayList<>();

    /**
     * Set the creation and update timestamps before persisting.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason", length = 30)
    private CancellationReason cancellationReason;

    /**
     * Update the modification timestamp before updating.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
