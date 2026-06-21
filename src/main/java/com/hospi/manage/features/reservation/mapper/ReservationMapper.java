package com.hospi.manage.features.reservation.mapper;

import com.hospi.manage.features.reservation.dto.ManageReservationView;
import com.hospi.manage.features.reservation.dto.ReservationListItemView;
import com.hospi.manage.features.reservation.dto.ReservationListView;
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

public class ReservationMapper {

    private ReservationMapper() {
    }

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

    public static ReservationListView toListView(
            List<Reservation> checkedIn,
            List<Reservation> active,
            String filterStatus,
            LocalDate filterDate,
            String filterSearch,
            String checkedInSearch) {
        return new ReservationListView(
                toListItemViews(checkedIn),
                toListItemViews(active),
                filterStatus,
                filterDate,
                filterSearch,
                checkedInSearch,
                0, 0, 0, 0, 0, 0);
    }

    public static ReservationListView toListView(
            Page<Reservation> checkedIn,
            Page<Reservation> active,
            String filterStatus,
            LocalDate filterDate,
            String filterSearch,
            String checkedInSearch) {
        return new ReservationListView(
                toListItemViews(checkedIn.getContent()),
                toListItemViews(active.getContent()),
                filterStatus,
                filterDate,
                filterSearch,
                checkedInSearch,
                checkedIn.getNumber(),
                checkedIn.getTotalPages(),
                checkedIn.getTotalElements(),
                active.getNumber(),
                active.getTotalPages(),
                active.getTotalElements());
    }

    public static List<ReservationListItemView> toListItemViews(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationMapper::toListItemView)
                .toList();
    }

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
