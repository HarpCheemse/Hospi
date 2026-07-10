package com.hospi.manage.features.guest.controller;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.guest.service.BookingFlowService;
import com.hospi.manage.features.guest.service.BookingFlowService.AvailabilityView;
import com.hospi.manage.features.guest.validation.BookingDateValidator;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.hospi.manage.common.constant.Attributes.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingFlowController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private BookingFlowService bookingFlowService;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private BookingDateValidator bookingDateValidator;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void createPayPalOrder_shouldRedirectToVerify_whenNoDraft() throws Exception {
        BookingDraft draft = new BookingDraft();

        mockMvc.perform(post("/book/pay").sessionAttr(BOOKING_DRAFT,
                draft)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/verify"));
    }

    @Test
    void createPayPalOrder_shouldRedirectToRooms_whenNotAvailable() throws Exception {
        BookingDraft draft = new BookingDraft();
        draft.setGuest(new BookingDraft.BookingGuest(null,
                "test@example.com",
                null,
                null,
                null));
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(new BookingDraft.RoomSelection(1L,
                "Deluxe",
                1,
                java.math.BigDecimal.valueOf(150))),
                null,
                null,
                0));
        when(otpService.isValidToken("some-token")).thenReturn(true);
        when(bookingFlowService.createPendingReservation(any(),
                any())).thenThrow(new IllegalStateException("Not available"));

        mockMvc.perform(post("/book/pay").sessionAttr(BOOKING_DRAFT,
                draft).sessionAttr("otpToken",
                "some-token")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/rooms")).andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void createPayPalOrder_shouldCreatePendingAndRedirectToPayPal() throws Exception {
        BookingDraft draft = new BookingDraft();
        draft.setGuest(new BookingDraft.BookingGuest(null,
                "test@example.com",
                null,
                null,
                null));
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(new BookingDraft.RoomSelection(1L,
                "Deluxe",
                1,
                java.math.BigDecimal.valueOf(150))),
                null,
                java.math.BigDecimal.valueOf(100),
                0));
        when(otpService.isValidToken("some-token")).thenReturn(true);
        Reservation reservation = new Reservation();
        reservation.setId(42L);
        when(bookingFlowService.createPendingReservation(any(),
                any())).thenReturn(reservation);
        when(paymentService.createOnlineBookingPayment(any(),
                any(),
                any())).thenReturn("https://paypal.com/checkout?token=TOKEN123");

        mockMvc.perform(post("/book/pay").sessionAttr(BOOKING_DRAFT,
                draft).sessionAttr("otpToken",
                "some-token")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("https://paypal.com/checkout?token=TOKEN123"));
    }

    @Test
    void payPalSuccess_shouldRedirectToPay_whenCaptureFails() throws Exception {
        when(paymentService.captureOnlineBookingPayment("ORDER-123")).thenReturn(false);

        mockMvc.perform(get("/book/pay/success").param("token",
                "ORDER-123").param("key",
                "any-key")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/pay")).andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void payPalSuccess_shouldRedirectToBook_whenNoPendingReservation() throws Exception {
        when(paymentService.captureOnlineBookingPayment("ORDER-123")).thenReturn(true);
        when(reservationService.findByPaymentIdempotencyKey("some-key")).thenThrow(new ResourceNotFoundException("Reservation"));

        mockMvc.perform(get("/book/pay/success").param("token",
                "ORDER-123").param("key",
                "some-key")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book")).andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void payPalSuccess_shouldRedirectToBook_whenReservationNotPending() throws Exception {
        when(paymentService.captureOnlineBookingPayment("ORDER-123")).thenReturn(true);
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationService.findByPaymentIdempotencyKey("some-key")).thenReturn(reservation);

        mockMvc.perform(get("/book/pay/success").param("token",
                "ORDER-123").param("key",
                "some-key")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book")).andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void payPalSuccess_shouldRefund_whenReservationCancelled() throws Exception {
        when(paymentService.captureOnlineBookingPayment("ORDER-123")).thenReturn(true);
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationService.findByPaymentIdempotencyKey("some-key")).thenReturn(reservation);
        when(paymentService.refundOnlineBookingPayment("ORDER-123")).thenReturn(true);

        mockMvc.perform(get("/book/pay/success").param("token",
                "ORDER-123").param("key",
                "some-key")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book")).andExpect(flash().attributeExists(ERROR));

        verify(paymentService).refundOnlineBookingPayment("ORDER-123");
    }

    @Test
    void payPalSuccess_shouldConfirmAndRedirectToConfirmation() throws Exception {
        when(paymentService.captureOnlineBookingPayment("ORDER-123")).thenReturn(true);
        Reservation reservation = new Reservation();
        reservation.setId(42L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setTotalPrice(java.math.BigDecimal.valueOf(200));
        reservation.setConfirmationCode("HSP-ABCDEF");
        when(reservationService.findByPaymentIdempotencyKey("some-key")).thenReturn(reservation);

        BookingDraft draft = new BookingDraft();
        draft.setRooms(new BookingDraft.BookingRooms(null,
                null,
                java.math.BigDecimal.valueOf(200),
                0));

        mockMvc.perform(get("/book/pay/success").param("token",
                "ORDER-123").param("key",
                "some-key").sessionAttr(BOOKING_DRAFT,
                draft).sessionAttr("otpToken",
                "some-token")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/confirmation?code=HSP-ABCDEF"));

        verify(reservationService).confirmAndAddPayment(42L,
                java.math.BigDecimal.valueOf(200),
                "ONLINE_BOOKING",
                "ORDER-123");
    }

    @Test
    void payPalCancel_shouldCancelPendingAndRedirect() throws Exception {
        Reservation reservation = new Reservation();
        reservation.setId(42L);
        reservation.setStatus(ReservationStatus.PENDING);
        when(reservationService.findByPaymentIdempotencyKey("some-key")).thenReturn(reservation);

        mockMvc.perform(get("/book/pay/cancel").param("key",
                "some-key")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/pay"));

        verify(reservationService).cancelPendingReservation(42L);
    }

    @Test
    void payPalCancel_shouldRedirect_whenNoPendingReservation() throws Exception {
        mockMvc.perform(get("/book/pay/cancel")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/pay"));

        verify(reservationService,
                never()).cancelPendingReservation(any());
    }

    @Test
    void submitRooms_shouldRedirectToBook_whenDraftDatesNull() throws Exception {
        mockMvc.perform(post("/book/rooms").sessionAttr(BOOKING_DRAFT,
                new BookingDraft())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book"));
    }

    @Test
    void submitRooms_shouldRedirectToRooms_whenParamsNull() throws Exception {
        BookingDraft draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));

        doThrow(new IllegalArgumentException("Please select at least one room.")).when(bookingFlowService).processRoomSelections(any(),
                any(),
                any());

        mockMvc.perform(post("/book/rooms").sessionAttr(BOOKING_DRAFT,
                draft)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/rooms")).andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void submitRooms_shouldRedirectToVerify_whenValid() throws Exception {
        BookingDraft draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));

        doNothing().when(bookingFlowService).processRoomSelections(any(),
                any(),
                any());

        mockMvc.perform(post("/book/rooms").sessionAttr(BOOKING_DRAFT,
                draft).param("roomTypeIds",
                "1").param("counts",
                "2")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/verify"));
    }

    @Test
    void showDateForm_shouldRender() throws Exception {
        mockMvc.perform(get("/book")).andExpect(status().isOk()).andExpect(view().name("guest/booking/book")).andExpect(model().attributeExists(FORM)).andExpect(content().string(containsString("Select Your Dates")));
    }

    @Test
    void showDateForm_shouldRedirectToPay_whenPaymentInProgress() throws Exception {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(new BookingDraft.RoomSelection(1L,
                "Deluxe",
                1,
                java.math.BigDecimal.valueOf(150))),
                java.math.BigDecimal.valueOf(750),
                java.math.BigDecimal.valueOf(150),
                5));
        draft.setGuest(new BookingDraft.BookingGuest("Jane",
                "jane@test.com",
                null,
                null,
                null));
        when(otpService.isValidToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/book").sessionAttr(BOOKING_DRAFT,
                draft).sessionAttr("otpToken",
                "valid-token")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/pay"));
    }

    @Test
    void showDateForm_shouldRender_whenReset() throws Exception {
        var draft = new BookingDraft();
        draft.setGuest(new BookingDraft.BookingGuest("Jane",
                "jane@test.com",
                null,
                null,
                null));
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(),
                null,
                null,
                5));
        when(otpService.isValidToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/book").param("reset",
                "true").sessionAttr(BOOKING_DRAFT,
                draft).sessionAttr("otpToken",
                "valid-token")).andExpect(status().isOk()).andExpect(view().name("guest/booking/book")).andExpect(model().attributeExists(FORM));
    }

    @Test
    void showDateForm_shouldCancelPending_whenReset() throws Exception {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.PENDING);
        when(reservationService.findByPaymentIdempotencyKey("some-key")).thenReturn(reservation);

        mockMvc.perform(get("/book").param("reset",
                "true").sessionAttr("pendingPaymentKey",
                "some-key")).andExpect(status().isOk()).andExpect(view().name("guest/booking/book")).andExpect(model().attributeExists(FORM));

        verify(reservationService).cancelPendingReservation(reservation.getId());
    }

    @Test
    void showRooms_shouldRender_whenDatesSet() throws Exception {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));

        when(bookingFlowService.buildAvailabilityView(any())).thenReturn(new AvailabilityView(List.of(),
                5,
                java.math.BigDecimal.valueOf(20),
                5));

        mockMvc.perform(get("/book/rooms").sessionAttr(BOOKING_DRAFT,
                draft)).andExpect(status().isOk()).andExpect(view().name("guest/booking/rooms")).andExpect(model().attributeExists(AVAILABILITY,
                MAX_ROOMS,
                DEPOSIT_PERCENTAGE,
                DRAFT)).andExpect(model().attribute("nights",
                5L)).andExpect(content().string(containsString("Choose Your Rooms")));
    }

    @Test
    void showRooms_shouldRedirect_whenNoDates() throws Exception {
        mockMvc.perform(get("/book/rooms").sessionAttr(BOOKING_DRAFT,
                new BookingDraft())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book"));
    }

    @Test
    void showVerify_shouldRender_whenRoomsSet() throws Exception {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(new BookingDraft.RoomSelection(1L,
                "Deluxe",
                1,
                java.math.BigDecimal.valueOf(150))),
                java.math.BigDecimal.valueOf(750),
                java.math.BigDecimal.valueOf(150),
                5));
        draft.setGuest(new BookingDraft.BookingGuest("Jane",
                "jane@test.com",
                null,
                null,
                null));

        mockMvc.perform(get("/book/verify").sessionAttr(BOOKING_DRAFT,
                draft)).andExpect(status().isOk()).andExpect(view().name("guest/booking/verify")).andExpect(model().attributeExists(DRAFT)).andExpect(content().string(containsString("Guest Details")));
    }

    @Test
    void showVerify_shouldRedirect_whenNoRooms() throws Exception {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));

        mockMvc.perform(get("/book/verify").sessionAttr(BOOKING_DRAFT,
                draft)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/rooms"));
    }

    @Test
    void showVerifyOtp_shouldRender_whenEmailSet() throws Exception {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(),
                null,
                null,
                5));
        draft.setGuest(new BookingDraft.BookingGuest("Jane",
                "jane@test.com",
                null,
                null,
                null));

        mockMvc.perform(get("/book/verify-otp").sessionAttr(BOOKING_DRAFT,
                draft)).andExpect(status().isOk()).andExpect(view().name("guest/booking/verify-otp")).andExpect(content().string(containsString("jane@test.com")));
    }

    @Test
    void showVerifyOtp_shouldRedirect_whenNoEmail() throws Exception {
        mockMvc.perform(get("/book/verify-otp").sessionAttr(BOOKING_DRAFT,
                new BookingDraft())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/verify"));
    }

    @Test
    void showPay_shouldRender_whenOtpVerified() throws Exception {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(new BookingDraft.RoomSelection(1L,
                "Deluxe",
                1,
                java.math.BigDecimal.valueOf(150))),
                java.math.BigDecimal.valueOf(750),
                java.math.BigDecimal.valueOf(150),
                5));
        draft.setGuest(new BookingDraft.BookingGuest("Jane",
                "jane@test.com",
                null,
                null,
                null));

        when(otpService.isValidToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/book/pay").sessionAttr(BOOKING_DRAFT,
                draft).sessionAttr("otpToken",
                "valid-token")).andExpect(status().isOk()).andExpect(view().name("guest/booking/pay")).andExpect(model().attributeExists(DRAFT)).andExpect(content().string(containsString("Jane"))).andExpect(content().string(containsString("jane@test.com"))).andExpect(content().string(containsString("Deluxe")));
    }

    @Test
    void showPay_shouldRedirect_whenOtpTokenMissing() throws Exception {
        var draft = new BookingDraft();
        draft.setGuest(new BookingDraft.BookingGuest("Jane",
                "jane@test.com",
                null,
                null,
                null));

        mockMvc.perform(get("/book/pay").sessionAttr(BOOKING_DRAFT,
                draft)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/verify-otp"));
    }

    @Test
    void showPay_shouldRedirect_whenOtpTokenInvalid() throws Exception {
        var draft = new BookingDraft();
        draft.setGuest(new BookingDraft.BookingGuest("Jane",
                "jane@test.com",
                null,
                null,
                null));
        when(otpService.isValidToken("expired-token")).thenReturn(false);

        mockMvc.perform(get("/book/pay").sessionAttr(BOOKING_DRAFT,
                draft).sessionAttr("otpToken",
                "expired-token")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/book/verify-otp"));
    }

    @Test
    void showConfirmation_shouldRender() throws Exception {
        mockMvc.perform(get("/book/confirmation").param("code",
                "HSP-ABCDEF")).andExpect(status().isOk()).andExpect(view().name("guest/booking/confirmation")).andExpect(model().attributeExists(BOOKING_CODE)).andExpect(content().string(containsString("HSP-ABCDEF")));
    }

    // --- POST /book (submitDates) ---

    @Test
    void submitDates_shouldReRender_whenBindingErrors() throws Exception {
        mockMvc.perform(post("/book")
                        .param("checkInAt", "")
                        .param("checkOutAt", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/booking/book"));
    }

    @Test
    void submitDates_shouldRedirectToRooms_whenValid() throws Exception {
        mockMvc.perform(post("/book")
                        .param("checkInAt", LocalDate.now().plusDays(1).toString())
                        .param("checkOutAt", LocalDate.now().plusDays(5).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/rooms"));
    }

    // --- POST /verify/send-otp (sendOtp) ---

    @Test
    void sendOtp_shouldReRender_whenBindingErrors() throws Exception {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5), LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(new BookingDraft.RoomSelection(1L, "Deluxe", 1, java.math.BigDecimal.valueOf(150))), java.math.BigDecimal.valueOf(750), java.math.BigDecimal.valueOf(150), 5));

        mockMvc.perform(post("/book/verify/send-otp")
                        .sessionAttr(BOOKING_DRAFT, draft)
                        .param("guestName", "")
                        .param("guestEmail", "")
                        .param("guestPhone", "")
                        .param("guestDateOfBirth", "")
                        .param("guestNationality", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/booking/verify"));
    }

    @Test
    void sendOtp_shouldRedirectWithError_whenRateLimited() throws Exception {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5), LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(new BookingDraft.RoomSelection(1L, "Deluxe", 1, java.math.BigDecimal.valueOf(150))), java.math.BigDecimal.valueOf(750), java.math.BigDecimal.valueOf(150), 5));

        when(bookingFlowService.initiateBookingOtp(any())).thenReturn(null);

        mockMvc.perform(post("/book/verify/send-otp")
                        .sessionAttr(BOOKING_DRAFT, draft)
                        .param("guestName", "Jane")
                        .param("guestEmail", "jane@test.com")
                        .param("guestPhone", "1234567890")
                        .param("guestDateOfBirth", "1990-01-01")
                        .param("guestNationality", "US")
                        .param("acceptedTos", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/verify"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void sendOtp_shouldRedirectToVerifyOtp_whenValid() throws Exception {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.now().plusDays(5), LocalDate.now().plusDays(10)));
        draft.setRooms(new BookingDraft.BookingRooms(List.of(new BookingDraft.RoomSelection(1L, "Deluxe", 1, java.math.BigDecimal.valueOf(150))), java.math.BigDecimal.valueOf(750), java.math.BigDecimal.valueOf(150), 5));

        when(bookingFlowService.initiateBookingOtp(any())).thenReturn("j***@test.com");

        mockMvc.perform(post("/book/verify/send-otp")
                        .sessionAttr(BOOKING_DRAFT, draft)
                        .param("guestName", "Jane")
                        .param("guestEmail", "jane@test.com")
                        .param("guestPhone", "1234567890")
                        .param("guestDateOfBirth", "1990-01-01")
                        .param("guestNationality", "US")
                        .param("acceptedTos", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/verify-otp"))
                .andExpect(flash().attributeExists(SUCCESS));
    }

    // --- POST /verify-otp (verifyOtp) ---

    @Test
    void verifyOtp_shouldRedirectToVerify_whenNoGuest() throws Exception {
        mockMvc.perform(post("/book/verify-otp")
                        .sessionAttr(BOOKING_DRAFT, new BookingDraft())
                        .param("otp", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/verify"));
    }

    @Test
    void verifyOtp_shouldReRender_whenBindingErrors() throws Exception {
        var draft = new BookingDraft();
        draft.setGuest(new BookingDraft.BookingGuest("Jane", "jane@test.com", null, null, null));

        mockMvc.perform(post("/book/verify-otp")
                        .sessionAttr(BOOKING_DRAFT, draft)
                        .param("otp", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/booking/verify-otp"))
                .andExpect(model().attributeExists(OTP_FORM));
    }

    @Test
    void verifyOtp_shouldRedirectWithError_whenOtpInvalid() throws Exception {
        var draft = new BookingDraft();
        draft.setGuest(new BookingDraft.BookingGuest("Jane", "jane@test.com", null, null, null));

        when(bookingFlowService.verifyBookingOtp("jane@test.com", "000000")).thenReturn(null);

        mockMvc.perform(post("/book/verify-otp")
                        .sessionAttr(BOOKING_DRAFT, draft)
                        .param("otp", "000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/verify-otp"))
                .andExpect(flash().attributeExists(ERROR));
    }

    @Test
    void verifyOtp_shouldRedirectToPay_whenValid() throws Exception {
        var draft = new BookingDraft();
        draft.setGuest(new BookingDraft.BookingGuest("Jane", "jane@test.com", null, null, null));

        when(bookingFlowService.verifyBookingOtp("jane@test.com", "123456")).thenReturn("valid-token");

        mockMvc.perform(post("/book/verify-otp")
                        .sessionAttr(BOOKING_DRAFT, draft)
                        .param("otp", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/pay"))
                .andExpect(request().sessionAttribute("otpToken", "valid-token"));
    }
}
