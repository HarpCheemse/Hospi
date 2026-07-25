package com.hospi.manage.features.hotel.dto;

import com.hospi.manage.features.hotel.entity.HotelPicture;
import com.hospi.manage.features.hotel.enums.HotelStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
/** Form for updating hotel details and settings. */
public class HotelForm {

    @NotBlank(message = "Hotel name is required")
    @Size(max = 255, message = "Hotel name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotBlank(message = "Phone number is required")
    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    @Email(message = "Invalid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotNull(message = "Check-in time is required")
    private LocalTime checkInTime;

    @NotNull(message = "Check-out time is required")
    private LocalTime checkOutTime;

    @Size(max = 1000, message = "Features must not exceed 1000 characters")
    private String features;

    @NotNull(message = "Status is required")
    private HotelStatus status;

    private List<HotelPicture> pictures;
}
