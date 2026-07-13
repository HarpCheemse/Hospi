package com.hospi.manage.features.reservation.mapper;

import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationMapperTest {

    private Reservation createReservation(ReservationStatus status) {
        var r = new Reservation();
        r.setId(1L);
        r.setGuestName("John Doe");
        r.setGuestEmail("john@test.com");
        r.setGuestPhone("+84 123");
        r.setCheckInAt(LocalDate.of(2026, 7, 1));
        r.setCheckOutAt(LocalDate.of(2026, 7, 4));
        r.setTotalPrice(BigDecimal.valueOf(600));
        r.setStatus(status);
        r.setSource(BookingSource.OFFLINE);
        r.setDetails(new ArrayList<>());
        return r;
    }

    private RoomType createRoomType(Long id, String name) {
        var t = new RoomType();
        t.setId(id);
        t.setName(name);
        return t;
    }

    private Room createRoom(Long id, String number, Short floor, RoomType type) {
        var r = new Room();
        r.setId(id);
        r.setRoomNumber(number);
        r.setFloorNumber(floor);
        r.setRoomType(type);
        return r;
    }

    // --- toManageView ---

    @Test
    void shouldMapToManageView() {
        var reservation = createReservation(ReservationStatus.CHECKED_IN);
        var deluxe = createRoomType(1L, "Deluxe");
        var superior = createRoomType(2L, "Superior");

        var guests = List.of(
                createGuest("Alice"),
                createGuest("Bob"));

        var assigned1 = createAssignment(10L, createRoom(101L, "201", (short) 2, deluxe));
        var assigned2 = createAssignment(11L, createRoom(102L, "202", (short) 2, deluxe));
        var assigned3 = createAssignment(12L, createRoom(103L, "301", (short) 3, superior));

        var available = List.of(
                createRoom(201L, "203", (short) 2, deluxe),
                createRoom(202L, "302", (short) 3, superior));

        var view = ReservationMapper.toManageView(reservation, guests, List.of(assigned1, assigned2, assigned3), available);

        assertEquals("John Doe", view.reservation().guestName());
        assertEquals(2, view.guests().size());
        assertEquals(3, view.assignedRooms().size());
        assertEquals(2, view.availableRooms().size());
        assertEquals(2, view.assignedCount(1L));
        assertEquals(1, view.assignedCount(2L));
        assertEquals(0, view.assignedCount(99L));
    }

    @Test
    void shouldMapEmptyManageView() {
        var reservation = createReservation(ReservationStatus.CHECKED_IN);

        var view = ReservationMapper.toManageView(reservation, List.of(), List.of(), List.of());

        assertTrue(view.guests().isEmpty());
        assertTrue(view.assignedRooms().isEmpty());
        assertTrue(view.availableRooms().isEmpty());
        assertTrue(view.assignedCounts().isEmpty());
    }

    // --- toListItemView ---

    @Test
    void shouldMapListItem() {
        var r = createReservation(ReservationStatus.PENDING);

        var detailType = createRoomType(1L, "Deluxe");
        var detail = new ReservationDetail();
        detail.setRoomType(detailType);
        detail.setRoomCount(2);
        r.setDetails(List.of(detail));

        var item = ReservationMapper.toListItemView(r);

        assertEquals(1L, item.id());
        assertEquals("John Doe", item.guestName());
        assertEquals(ReservationStatus.PENDING, item.status());
        assertEquals(BookingSource.OFFLINE, item.source());
        assertTrue(item.showPayButton());
        assertFalse(item.showCheckInButton());
        assertFalse(item.showViewButton());
        assertEquals(1, item.details().size());
        assertEquals("Deluxe", item.details().get(0).roomTypeName());
    }

    @Test
    void shouldShowCheckInButton_whenConfirmed() {
        var r = createReservation(ReservationStatus.CONFIRMED);
        var item = ReservationMapper.toListItemView(r);
        assertFalse(item.showPayButton());
        assertTrue(item.showCheckInButton());
        assertFalse(item.showViewButton());
    }

    @Test
    void shouldShowViewButton_whenOnlinePending() {
        var r = createReservation(ReservationStatus.PENDING);
        r.setSource(BookingSource.ONLINE);
        var item = ReservationMapper.toListItemView(r);
        assertFalse(item.showPayButton());
        assertFalse(item.showCheckInButton());
        assertTrue(item.showViewButton());
    }

    // --- toActiveBookingsView / toCurrentStaysView ---

    @Test
    void shouldMapActiveBookingsView() {
        var r = createReservation(ReservationStatus.PENDING);
        var t = createRoomType(1L, "Standard");
        r.setDetails(List.of(createDetail(t, 1)));

        Page<Reservation> page = new PageImpl<>(List.of(r), PageRequest.of(1, 10), 1);
        var view = ReservationMapper.toActiveBookingsView(page, "PENDING", null, null);

        assertEquals(1, view.bookings().size());
        assertEquals("PENDING", view.filterStatus());
        assertEquals(1, view.page());
    }

    @Test
    void shouldMapCurrentStaysView() {
        var r = createReservation(ReservationStatus.CHECKED_IN);
        var t = createRoomType(1L, "Standard");
        r.setDetails(List.of(createDetail(t, 2)));

        Page<Reservation> page = new PageImpl<>(List.of(r), PageRequest.of(0, 10), 1);
        var view = ReservationMapper.toCurrentStaysView(page, "search-term");

        assertEquals(1, view.guests().size());
        assertEquals("search-term", view.checkedInSearch());
    }

    // --- helpers ---

    private StayingGuest createGuest(String name) {
        var g = new StayingGuest();
        g.setGuestName(name);
        g.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return g;
    }

    private RoomAssignment createAssignment(Long id, Room room) {
        var a = new RoomAssignment();
        a.setId(id);
        a.setRoom(room);
        return a;
    }

    private ReservationDetail createDetail(RoomType type, int count) {
        var d = new ReservationDetail();
        d.setRoomType(type);
        d.setRoomCount(count);
        return d;
    }
}
