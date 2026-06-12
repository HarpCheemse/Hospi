package com.hospi.manage.features.guest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class GuestDetailForm {

    @NotBlank(message = "Full name is required")
    private String guestName;

    @NotBlank(message = "Email is required")
    private String guestEmail;

    @NotBlank(message = "Phone is required")
    private String guestPhone;

    @NotNull(message = "Date of birth is required")
    private LocalDate guestDateOfBirth;

    private String guestNationality;

    public GuestDetailForm() {
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
}
