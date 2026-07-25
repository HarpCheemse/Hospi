package com.hospi.manage.features.guest.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Session-scoped draft for the booking wizard. Fields fill one step at a time: dates → rooms → guest. */
@Getter
@Setter
public class BookingDraft {

    private BookingDates dates;
    private BookingRooms rooms;
    private BookingGuest guest;
    private boolean acceptedTos;

    public record BookingDates(LocalDate checkInAt, LocalDate checkOutAt) {
    }

    public record BookingRooms(List<RoomSelection> selections, BigDecimal totalPrice, BigDecimal depositAmount,
                               long numberOfNights) {
    }

    public record BookingGuest(String name, String email, String phone, LocalDate dateOfBirth, String nationality) {
    }

    public record RoomSelection(Long roomTypeId, String roomTypeName, int count, BigDecimal basePrice) {
    }
}
