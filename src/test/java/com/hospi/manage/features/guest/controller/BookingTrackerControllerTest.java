package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.guest.service.BookingTrackerService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.hospi.manage.common.constant.Attributes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingTrackerController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingTrackerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingTrackerService bookingTrackerService;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private com.hospi.manage.features.notification.service.NotificationService notificationService;

    @MockitoBean
    private EmailService emailService;

    @Test
    void myBooking_shouldShowTrackForm_whenNoTrackedCodes() throws Exception {
        mockMvc.perform(get("/my-booking"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/my-booking"))
                .andExpect(model().attributeExists(FORM));
    }

    @Test
    void myBooking_shouldShowReservations_whenTrackedCodesExist() throws Exception {
        Reservation res = new Reservation();
        res.setId(1L);
        res.setGuestName("Guest");
        res.setConfirmationCode("CODE");
        res.setStatus(ReservationStatus.CONFIRMED);
        res.setCheckInAt(java.time.LocalDate.now());
        res.setCheckOutAt(java.time.LocalDate.now().plusDays(2));
        res.setSource(BookingSource.ONLINE);
        when(bookingTrackerService.resolveByCodes(anySet())).thenReturn(java.util.List.of(res));
        when(bookingTrackerService.buildReviewMap(anyList())).thenReturn(Map.of());
        when(bookingTrackerService.buildPillClasses(anyList())).thenReturn(Map.of());

        mockMvc.perform(get("/my-booking")
                        .sessionAttr(TRACKED_BOOKING_CODES, Set.of("CODE")))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/my-booking"))
                .andExpect(model().attributeExists(RESERVATIONS));
    }

    @Test
    void verifyPage_shouldRender_whenSessionHasEmail() throws Exception {
        mockMvc.perform(get("/my-booking/verify")
                        .sessionAttr(TRACKED_EMAIL, "a@b.com")
                        .sessionAttr(PENDING_CODE, "CODE"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/my-booking-verify"))
                .andExpect(model().attribute(VERIFY_EMAIL, "a@b.com"))
                .andExpect(model().attributeExists(OTP_FORM));
    }

    @Test
    void verifyPage_shouldRedirect_whenNoSession() throws Exception {
        mockMvc.perform(get("/my-booking/verify"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-booking"));
    }

    @Test
    void lookup_shouldReturnForm_whenBindingErrors() throws Exception {
        mockMvc.perform(post("/my-booking/lookup").with(csrf())
                        .param("email", "")
                        .param("bookingCode", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/my-booking"));
    }

    @Test
    void lookup_shouldRedirectWithError_whenBookingNotFound() throws Exception {
        when(bookingTrackerService.lookupByEmailAndCode("a@b.com", "CODE"))
                .thenThrow(new com.hospi.manage.common.exception.ResourceNotFoundException("Reservation"));

        mockMvc.perform(post("/my-booking/lookup").with(csrf())
                        .param("email", "a@b.com")
                        .param("bookingCode", "CODE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-booking"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void lookup_shouldSendOtpAndRedirect_whenBookingFound() throws Exception {
        when(bookingTrackerService.lookupByEmailAndCode("a@b.com", "CODE")).thenReturn(new Reservation());
        when(otpService.createOtp("a@b.com", OtpType.BOOKING_TRACK)).thenReturn("123456");

        mockMvc.perform(post("/my-booking/lookup").with(csrf())
                        .param("email", "a@b.com")
                        .param("bookingCode", "CODE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-booking/verify"))
                .andExpect(request().sessionAttribute(TRACKED_EMAIL, "a@b.com"))
                .andExpect(request().sessionAttribute(PENDING_CODE, "CODE"));

        verify(emailService).send(eq("a@b.com"), anyString(), anyString());
    }

    @Test
    void verify_shouldRedirect_whenSessionMissing() throws Exception {
        mockMvc.perform(post("/my-booking/verify").with(csrf())
                        .param("otp", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-booking"));
    }

    @Test
    void verify_shouldReturnForm_whenOtpBlank() throws Exception {
        mockMvc.perform(post("/my-booking/verify").with(csrf())
                        .sessionAttr(TRACKED_EMAIL, "a@b.com")
                        .sessionAttr(PENDING_CODE, "CODE")
                        .param("otp", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/my-booking-verify"))
                .andExpect(model().attribute(VERIFY_EMAIL, "a@b.com"));
    }

    @Test
    void verify_shouldRedirectWithError_whenOtpInvalid() throws Exception {
        when(otpService.verifyOtp("a@b.com", "000000", OtpType.BOOKING_TRACK)).thenReturn(false);

        mockMvc.perform(post("/my-booking/verify").with(csrf())
                        .sessionAttr(TRACKED_EMAIL, "a@b.com")
                        .sessionAttr(PENDING_CODE, "CODE")
                        .param("otp", "000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-booking/verify"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void verify_shouldAddCodeToSession_whenOtpValid() throws Exception {
        when(otpService.verifyOtp("a@b.com", "123456", OtpType.BOOKING_TRACK)).thenReturn(true);

        mockMvc.perform(post("/my-booking/verify").with(csrf())
                        .sessionAttr(TRACKED_EMAIL, "a@b.com")
                        .sessionAttr(PENDING_CODE, "CODE")
                        .sessionAttr(TRACKED_BOOKING_CODES, new LinkedHashSet<>())
                        .param("otp", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-booking"))
                .andExpect(flash().attributeExists(SUCCESS));
    }

    @Test
    void clear_shouldRemoveSessionAttributes() throws Exception {
        mockMvc.perform(post("/my-booking/clear").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-booking"));
    }

    @Test
    void submitReview_shouldRenderForm_whenSessionEmpty() throws Exception {
        mockMvc.perform(post("/my-booking/review").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/my-booking"))
                .andExpect(model().attributeExists(FORM));
    }

    @Test
    void submitReview_shouldSaveAndRedirect_whenValid() throws Exception {
        Reservation res = new Reservation();
        res.setId(1L);
        when(bookingTrackerService.resolveByCodes(anySet())).thenReturn(java.util.List.of(res));
        when(bookingTrackerService.buildReviewMap(anyList())).thenReturn(Map.of());
        when(bookingTrackerService.buildPillClasses(anyList())).thenReturn(Map.of());

        mockMvc.perform(post("/my-booking/review").with(csrf())
                        .sessionAttr(TRACKED_BOOKING_CODES, Set.of("CODE"))
                        .param("rating", "4")
                        .param("reservationId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-booking"));

        verify(bookingTrackerService).submitReview(1L, 4, Set.of("CODE"));
    }
}
