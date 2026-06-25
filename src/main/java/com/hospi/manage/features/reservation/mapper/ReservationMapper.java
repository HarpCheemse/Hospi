package com.hospi.manage.features.reservation.mapper;

import com.hospi.manage.features.reservation.dto.response.ActiveBookingsView;
import com.hospi.manage.features.reservation.dto.response.CurrentStaysView;
import com.hospi.manage.features.reservation.dto.response.ManageReservationView;
import com.hospi.manage.features.reservation.dto.response.ReservationListItemView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.room.entity.Room;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Static mapping utilities for reservation entities to view models. */
public class ReservationMapper {

    private ReservationMapper() {
    }

    /**
     * Build a {@link ManageReservationView} combining the reservation, its guests, assigned rooms, and available rooms.
     *
     * @param reservation   the reservation to display
     * @param guests        guests associated with the reservation
     * @param assignedRooms rooms currently assigned
     * @param availableRooms rooms available for assignment
     * @return a composite manage-view model
     */
    public static ManageReservationView toManageView(
            Reservation reservation,
            List<StayingGuest> guests,
            List<RoomAssignment> assignedRooms,
            List<Room> availableRooms) {
        Map<Long, Integer> assignedCounts = assignedRooms.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getRoom().getRoomType().getId(),
                        Collectors.summingInt(a -> 1)));

        return new ManageReservationView(
                reservation, guests, assignedRooms, availableRooms, assignedCounts);
    }

    /**
     * Build an {@link ActiveBookingsView} from a paged result with current filter values.
     *
     * @param bookings     paged reservation data
     * @param filterStatus active status filter value
     * @param filterDate   active date filter value
     * @param filterSearch active search-term filter value
     * @return a paginated active-bookings view model
     */
    public static ActiveBookingsView toActiveBookingsView(
            Page<Reservation> bookings,
            String filterStatus,
            LocalDate filterDate,
            String filterSearch) {
        return new ActiveBookingsView(
                toListItemViews(bookings.getContent()),
                filterStatus,
                filterDate,
                filterSearch,
                bookings.getNumber(),
                bookings.getTotalPages(),
                bookings.getTotalElements());
    }

    /**
     * Build a {@link CurrentStaysView} from a paged result with the current search term.
     *
     * @param guests          paged reservation data for currently checked-in guests
     * @param checkedInSearch active search term
     * @return a paginated current-stays view model
     */
    public static CurrentStaysView toCurrentStaysView(
            Page<Reservation> guests,
            String checkedInSearch) {
        return new CurrentStaysView(
                toListItemViews(guests.getContent()),
                checkedInSearch,
                guests.getNumber(),
                guests.getTotalPages(),
                guests.getTotalElements());
    }

    /**
     * Convert a list of reservations to their list-item view representations.
     *
     * @param reservations reservations to transform
     * @return list of {@link ReservationListItemView}
     */
    public static List<ReservationListItemView> toListItemViews(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationMapper::toListItemView)
                .toList();
    }

    /**
     * Convert a single reservation to its list-item view representation.
     *
     * @param r the reservation to transform
     * @return a {@link ReservationListItemView}
     */
    public static ReservationListItemView toListItemView(Reservation r) {
        return new ReservationListItemView(
                r.getId(),
                r.getGuestName(),
                r.getGuestEmail(),
                r.getGuestPhone(),
                r.getCheckInAt(),
                r.getCheckOutAt(),
                r.getTotalPrice(),
                r.getStatus(),
                r.getSource(),
                r.getDetails(),
                r.getStatus() == ReservationStatus.PENDING && r.getSource() == BookingSource.OFFLINE,
                r.getStatus() == ReservationStatus.CONFIRMED,
                r.getStatus() == ReservationStatus.PENDING && r.getSource() == BookingSource.ONLINE
        );
    }
}
