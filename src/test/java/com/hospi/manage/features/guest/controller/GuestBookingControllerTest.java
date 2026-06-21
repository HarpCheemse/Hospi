package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.guest.validation.BookingDateValidator;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.room.dto.response.RoomTypeAvailability;
import com.hospi.manage.features.room.entity.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.hospi.manage.common.constant.Attributes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GuestBookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class GuestBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private RoomAvailabilityService roomAvailabilityService;

    @MockitoBean
    private SystemConfigService systemConfigService;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private BookingDateValidator bookingDateValidator;

    @Test
    void submitRooms_shouldRedirectToBook_whenDraftDatesNull() throws Exception {
        mockMvc.perform(post("/book/rooms")
                        .sessionAttr(BOOKING_DRAFT, new BookingDraft()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book"));
    }

    @Test
    void submitRooms_shouldRedirectToRooms_whenParamsNull() throws Exception {
        BookingDraft draft = new BookingDraft();
        draft.setCheckInAt(LocalDate.now().plusDays(5));
        draft.setCheckOutAt(LocalDate.now().plusDays(10));

        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(java.util.List.of());

        mockMvc.perform(post("/book/rooms")
                        .sessionAttr(BOOKING_DRAFT, draft))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/rooms"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void submitRooms_shouldRedirectToVerify_whenValid() throws Exception {
        BookingDraft draft = new BookingDraft();
        draft.setCheckInAt(LocalDate.now().plusDays(5));
        draft.setCheckOutAt(LocalDate.now().plusDays(10));

        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe");
        roomType.setBasePrice(java.math.BigDecimal.valueOf(150));
        roomType.setMaxOccupancy(2);
        roomType.setArea(32);
        roomType.setBedType(null);
        roomType.setFeatures("");
        roomType.setActive(true);
        when(roomAvailabilityService.getAvailability(any(), any()))
                .thenReturn(java.util.List.of(new RoomTypeAvailability(roomType, 5, 5)));
        var config = new com.hospi.manage.features.admin.config.entity.SystemConfig();
        config.setMaximumRoomPerBook(5);
        config.setDefaultDepositPercentage(java.math.BigDecimal.valueOf(20));
        when(systemConfigService.getConfig()).thenReturn(config);

        mockMvc.perform(post("/book/rooms")
                        .sessionAttr(BOOKING_DRAFT, draft)
                        .param("roomTypeIds", "1")
                        .param("counts", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/verify"));
    }
}
