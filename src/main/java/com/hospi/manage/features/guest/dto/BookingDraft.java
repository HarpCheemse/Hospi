package com.hospi.manage.features.guest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private boolean emailVerified;
    private String paypalOrderId;

    public BookingDraft() {
    }

    public LocalDate getCheckInAt() {
        return checkInAt;
    }

    public void setCheckInAt(LocalDate checkInAt) {
        this.checkInAt = checkInAt;
    }

    public LocalDate getCheckOutAt() {
        return checkOutAt;
    }

    public void setCheckOutAt(LocalDate checkOutAt) {
        this.checkOutAt = checkOutAt;
    }

    public List<RoomSelection> getRoomSelections() {
        return roomSelections;
    }

    public void setRoomSelections(List<RoomSelection> roomSelections) {
        this.roomSelections = roomSelections;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    public LocalDate getGuestDateOfBirth() {
        return guestDateOfBirth;
    }

    public void setGuestDateOfBirth(LocalDate guestDateOfBirth) {
        this.guestDateOfBirth = guestDateOfBirth;
    }

    public String getGuestNationality() {
        return guestNationality;
    }

    public void setGuestNationality(String guestNationality) {
        this.guestNationality = guestNationality;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public void setPaypalOrderId(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
    }

    public static record RoomSelection(
            Long roomTypeId,
            String roomTypeName,
            Integer count,
            BigDecimal basePrice
    ) {
    }
}
