package com.hospi.manage.features.manager.detail.dto;

import com.hospi.manage.features.manager.detail.entity.HotelPicture;
import com.hospi.manage.features.manager.detail.enums.HotelStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class HotelForm {

    @NotBlank(message = "Hotel name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "Check-in time is required")
    private LocalTime checkInTime;

    @NotNull(message = "Check-out time is required")
    private LocalTime checkOutTime;

    private String features;

    @NotNull(message = "Status is required")
    private HotelStatus status;

    @NotNull(message = "Term of service is required")
    private String termOfService;

    private List<HotelPicture> pictures;
}