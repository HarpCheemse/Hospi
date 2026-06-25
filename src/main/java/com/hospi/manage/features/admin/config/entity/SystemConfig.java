package com.hospi.manage.features.admin.config.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity representing hotel-level configuration settings (financial, booking rules, refunds).
 */
@Entity
@Table(name = "system_configs")
@Getter
@Setter
public class SystemConfig {

    @Id
    @Column(name = "hotel_id")
    private Long hotelId;

    // Financial

    @Column(name = "tax_rate", nullable = false)
    private BigDecimal taxRate;

    @Column(name = "default_deposit_percentage", nullable = false)
    private BigDecimal defaultDepositPercentage;

    @Column(name = "late_checkout_fee", nullable = false)
    private BigDecimal lateCheckoutFee;

    @Column(name = "extra_guest_fee", nullable = false)
    private BigDecimal extraGuestFee;

    // Booking Rules

    @Column(name = "pending_booking_expiry_minutes", nullable = false)
    private Integer pendingBookingExpiryMinutes;

    @Column(name = "cancellation_hours_before_checkin", nullable = false)
    private Integer cancellationHoursBeforeCheckin;

    @Column(name = "maximum_booking_days", nullable = false)
    private Integer maximumBookingDays;

    @Column(name = "maximum_room_per_book", nullable = false)
    private Integer maximumRoomPerBook;

    // Refund Settings

    @Column(name = "refund_percentage", nullable = false)
    private BigDecimal refundPercentage;

    @Column(name = "full_refund_window_hours", nullable = false)
    private Integer fullRefundWindowHours;
}