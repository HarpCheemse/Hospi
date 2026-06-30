package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** View model for a single reservation row in a list. */
public record ReservationListItemView(
    Long id,
    String guestName,
    String guestEmail,
    String guestPhone,
    LocalDate checkInAt,
    LocalDate checkOutAt,
    BigDecimal totalPrice,
    ReservationStatus status,
    BookingSource source,
    List<ReservationDetail> details,
    boolean showPayButton,
    boolean showCheckInButton,
    boolean showViewButton
) {}
