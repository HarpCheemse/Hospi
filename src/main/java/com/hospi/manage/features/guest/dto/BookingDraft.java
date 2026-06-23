package com.hospi.manage.features.guest.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BookingDraft {

    private LocalDate checkInAt;
    private LocalDate checkOutAt;
    private List<RoomSelection> roomSelections;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private LocalDate guestDateOfBirth;
    private String guestNationality;
    private BigDecimal totalPrice;
    private BigDecimal depositAmount;
    private Long numberOfNights;

    public static record RoomSelection(
            Long roomTypeId,
            String roomTypeName,
            Integer count,
            BigDecimal basePrice
    ) {
    }
}
