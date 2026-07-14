package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * View model for a single reservation row in a list.
 * No JPA entities are exposed.
 */
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
    List<ReservationDetailView> details,
    boolean showPayButton,
    boolean showCheckInButton,
    boolean showViewButton,
    Integer registeredGuestCount,
    Integer assignedRoomCount,
    Integer totalRoomCount
) {
    public static ReservationListItemView withCounts(ReservationListItemView base, int guests, int assigned, int total) {
        return new ReservationListItemView(
                base.id(), base.guestName(), base.guestEmail(), base.guestPhone(),
                base.checkInAt(), base.checkOutAt(), base.totalPrice(), base.status(),
                base.source(), base.details(), base.showPayButton(), base.showCheckInButton(),
                base.showViewButton(), guests, assigned, total);
    }
}
