package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.room.entity.RoomType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationSummaryViewTest {

    private Reservation createReservation() {
        var r = new Reservation();
        r.setId(1L);
        r.setGuestName("John Doe");
        r.setGuestEmail("john@test.com");
        r.setGuestPhone("+84 123");
        r.setGuestNationality("Vietnamese");
        r.setGuestDateOfBirth(LocalDate.of(1990, 6, 15));
        r.setCheckInAt(LocalDate.of(2026, 7, 1));
        r.setCheckOutAt(LocalDate.of(2026, 7, 4));
        r.setStatus(ReservationStatus.CONFIRMED);
        r.setSource(BookingSource.ONLINE);
        r.setConfirmationCode("HSP-ABC123");
        r.setTotalPrice(BigDecimal.valueOf(600));
        r.setCheckedOutAt(LocalDateTime.of(2026, 7, 4, 11, 0));
        r.setCheckedOutBy("receptionist");
        r.setLateCheckoutFeeApplied(null);
        r.setExtraGuestFeeApplied(null);
        r.setDetails(new ArrayList<>());
        return r;
    }

    @Test
    void shouldMapFromEntity() {
        var r = createReservation();
        var view = ReservationSummaryView.from(r);

        assertEquals(1L, view.id());
        assertEquals("John Doe", view.guestName());
        assertEquals("john@test.com", view.guestEmail());
        assertEquals("+84 123", view.guestPhone());
        assertEquals("Vietnamese", view.guestNationality());
        assertEquals(LocalDate.of(1990, 6, 15), view.guestDateOfBirth());
        assertEquals(LocalDate.of(2026, 7, 1), view.checkInAt());
        assertEquals(LocalDate.of(2026, 7, 4), view.checkOutAt());
        assertEquals(ReservationStatus.CONFIRMED, view.status());
        assertEquals(BookingSource.ONLINE, view.source());
        assertEquals("HSP-ABC123", view.confirmationCode());
        assertEquals(BigDecimal.valueOf(600), view.totalPrice());
        assertEquals(LocalDateTime.of(2026, 7, 4, 11, 0), view.checkedOutAt());
        assertEquals("receptionist", view.checkedOutBy());
        assertNull(view.lateCheckoutFeeApplied());
        assertNull(view.extraGuestFeeApplied());
        assertTrue(view.details().isEmpty());
    }

    @Test
    void shouldMapDetails() {
        var r = createReservation();
        var type = new RoomType();
        type.setId(2L);
        type.setName("Deluxe");

        var detail = new ReservationDetail();
        detail.setRoomType(type);
        detail.setRoomCount(1);
        detail.setBasePrice(BigDecimal.valueOf(200));
        detail.setTotalPrice(BigDecimal.valueOf(200));

        r.setDetails(List.of(detail));

        var view = ReservationSummaryView.from(r);

        assertEquals(1, view.details().size());
        assertEquals("Deluxe", view.details().get(0).roomTypeName());
    }

    @Test
    void shouldComputeNights() {
        var r = createReservation();
        var view = ReservationSummaryView.from(r);
        assertEquals(3, view.nights());
    }

    @Test
    void shouldComputeZeroNights_whenSameDay() {
        var r = createReservation();
        r.setCheckInAt(LocalDate.of(2026, 7, 1));
        r.setCheckOutAt(LocalDate.of(2026, 7, 1));
        var view = ReservationSummaryView.from(r);
        assertEquals(0, view.nights());
    }
}
