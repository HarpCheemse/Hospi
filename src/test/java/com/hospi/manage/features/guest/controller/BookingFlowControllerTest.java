package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.guest.validation.BookingDateValidator;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.service.AvailabilityService;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.room.dto.response.RoomTypeAvailability;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.hospi.manage.common.constant.Attributes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingFlowController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private RoomAvailabilityService roomAvailabilityService;

    @MockitoBean
    private com.hospi.manage.features.notification.service.NotificationService notificationService;

    @MockitoBean
    private SystemConfigService systemConfigService;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private BookingDateValidator bookingDateValidator;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private AvailabilityService availabilityService;

    @Test
    void createPayPalOrder_shouldRedirectToVerify_whenNoDraft() throws Exception {
        BookingDraft draft = new BookingDraft();

        mockMvc.perform(post("/book/pay")
                        .sessionAttr(BOOKING_DRAFT, draft))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/verify"));
    }

    @Test
    void createPayPalOrder_shouldRedirectToPay_whenPaymentInProgress() throws Exception {
        BookingDraft draft = new BookingDraft();
        draft.setGuestEmail("test@example.com");

        mockMvc.perform(post("/book/pay")
                        .sessionAttr(BOOKING_DRAFT, draft)
                        .sessionAttr("pendingReservationId", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/pay"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void createPayPalOrder_shouldRedirectToRooms_whenNotAvailable() throws Exception {
        BookingDraft draft = new BookingDraft();
        draft.setGuestEmail("test@example.com");
        draft.setCheckInAt(LocalDate.now().plusDays(5));
        draft.setCheckOutAt(LocalDate.now().plusDays(10));
        draft.setRoomSelections(List.of(new BookingDraft.RoomSelection(1L, "Deluxe", 1, java.math.BigDecimal.valueOf(150))));

        when(availabilityService.canFulfil(any(), any(), any(), isNull())).thenReturn(false);

        mockMvc.perform(post("/book/pay")
                        .sessionAttr(BOOKING_DRAFT, draft))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/rooms"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void createPayPalOrder_shouldCreatePendingAndRedirectToPayPal() throws Exception {
        BookingDraft draft = new BookingDraft();
        draft.setGuestEmail("test@example.com");
        draft.setCheckInAt(LocalDate.now().plusDays(5));
        draft.setCheckOutAt(LocalDate.now().plusDays(10));
        draft.setRoomSelections(List.of(new BookingDraft.RoomSelection(1L, "Deluxe", 1, java.math.BigDecimal.valueOf(150))));
        draft.setDepositAmount(java.math.BigDecimal.valueOf(100));

        when(availabilityService.canFulfil(any(), any(), any(), isNull())).thenReturn(true);
        Reservation reservation = new Reservation();
        reservation.setId(42L);
        when(reservationService.createOnlineBookingPending(any(), any())).thenReturn(reservation);
        when(paymentService.createOnlineBookingPayment(any(), any(), any())).thenReturn("https://paypal.com/checkout?token=TOKEN123");

        mockMvc.perform(post("/book/pay")
                        .sessionAttr(BOOKING_DRAFT, draft))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://paypal.com/checkout?token=TOKEN123"));
    }

    @Test
    void payPalSuccess_shouldRedirectToPay_whenCaptureFails() throws Exception {
        when(paymentService.captureOnlineBookingPayment("ORDER-123")).thenReturn(false);

        mockMvc.perform(get("/book/pay/success")
                        .param("token", "ORDER-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/pay"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void payPalSuccess_shouldRedirectToBook_whenNoPendingReservation() throws Exception {
        when(paymentService.captureOnlineBookingPayment("ORDER-123")).thenReturn(true);

        mockMvc.perform(get("/book/pay/success")
                        .param("token", "ORDER-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void payPalSuccess_shouldRedirectToBook_whenReservationNotPending() throws Exception {
        when(paymentService.captureOnlineBookingPayment("ORDER-123")).thenReturn(true);
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationService.findById(42L)).thenReturn(reservation);

        mockMvc.perform(get("/book/pay/success")
                        .param("token", "ORDER-123")
                        .sessionAttr("pendingReservationId", 42L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void payPalSuccess_shouldConfirmAndRedirectToConfirmation() throws Exception {
        when(paymentService.captureOnlineBookingPayment("ORDER-123")).thenReturn(true);
        Reservation reservation = new Reservation();
        reservation.setId(42L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setTotalPrice(java.math.BigDecimal.valueOf(200));
        reservation.setConfirmationCode("HSP-ABCDEF");
        when(reservationService.findById(42L)).thenReturn(reservation);

        mockMvc.perform(get("/book/pay/success")
                        .param("token", "ORDER-123")
                        .sessionAttr("pendingReservationId", 42L)
                        .sessionAttr(BOOKING_DRAFT, new BookingDraft())
                        .sessionAttr("otpToken", "some-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/confirmation?code=HSP-ABCDEF"));

        verify(reservationService).confirmAndAddPayment(42L, java.math.BigDecimal.valueOf(200), "ONLINE_BOOKING", "ORDER-123");
    }

    @Test
    void payPalCancel_shouldCancelPendingAndRedirect() throws Exception {
        mockMvc.perform(get("/book/pay/cancel")
                        .sessionAttr("pendingReservationId", 42L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/pay"));

        verify(reservationService).cancelPendingReservation(42L);
    }

    @Test
    void payPalCancel_shouldRedirect_whenNoPendingReservation() throws Exception {
        mockMvc.perform(get("/book/pay/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/pay"));

        verify(reservationService, never()).cancelPendingReservation(any());
    }

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
