package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.dto.request.*;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.CheckoutService;
import com.hospi.manage.features.reservation.service.RoomUpgradeService;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAssignmentService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import com.hospi.manage.features.reservation.validation.DateSearchValidator;
import com.hospi.manage.features.reservation.validation.OfflineBookingFormValidator;
import com.hospi.manage.features.room.dto.response.RoomTypeAvailability;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.BedType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.hospi.manage.common.constant.Attributes.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest({ReceptionistBookingController.class, ReceptionistStayController.class})
@AutoConfigureMockMvc(addFilters = false)
class ReceptionistReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private OfflineBookingFormValidator offlineBookingFormValidator;

    @MockitoBean
    private DateSearchValidator dateSearchValidator;

    @MockitoBean
    private RoomAvailabilityService roomAvailabilityService;

    @MockitoBean
    private StayingGuestService stayingGuestService;

    @MockitoBean
    private RoomAssignmentService roomAssignmentService;

    @MockitoBean
    private RoomUpgradeService roomUpgradeService;

    @MockitoBean
    private RoomTypeRepository roomTypeRepository;

    @MockitoBean
    private RoomRepository roomRepository;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private CheckoutService checkoutService;

    @MockitoBean
    private SystemConfigService systemConfigService;

    @MockitoBean
    private AuditService auditService;

    @BeforeEach
    void setUpSecurityContext() {
        Account account = new Account();
        account.setEmail("receptionist@hospi.com");
        account.setRole(Role.RECEPTIONIST);
        account.setActive(true);
        AccountPrincipal principal = new AccountPrincipal(account);
        var auth = new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(), principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Reservation createReservation(ReservationStatus status) {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setGuestName("John Doe");
        r.setGuestEmail("john@test.com");
        r.setCheckInAt(LocalDate.of(2026, 7, 1));
        r.setCheckOutAt(LocalDate.of(2026, 7, 5));
        r.setStatus(status);
        r.setSource(BookingSource.OFFLINE);
        return r;
    }

    private RoomType createRoomType(Long id, String name) {
        RoomType rt = new RoomType();
        rt.setId(id);
        rt.setName(name);
        rt.setMaxOccupancy(2);
        rt.setBasePrice(BigDecimal.valueOf(150));
        rt.setBedType(BedType.QUEEN);
        rt.setArea(30);
        rt.setFeatures("Wi-Fi, TV");
        rt.setActive(true);
        rt.setPictures(new ArrayList<>());
        return rt;
    }

    private void mockManageView(Reservation reservation) {
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(stayingGuestService.getGuests(1L)).thenReturn(List.of());
        when(stayingGuestService.getAdultGuestCount(anyLong(), any())).thenReturn(2);
        when(roomAssignmentService.getAssignedRooms(1L)).thenReturn(List.of());
        when(roomAssignmentService.getAvailableRooms(1L)).thenReturn(List.of());
    }

    // ========== GET / ==========

    @Test
    void activeBookings_shouldRender() throws Exception {
        var reservation = createReservation(ReservationStatus.PENDING);
        Page<Reservation> page = new PageImpl<>(
                List.of(reservation), PageRequest.of(0, 10), 1);
        when(reservationService.findFiltered(anyList(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/receptionist/bookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/active"))
                .andExpect(model().attributeExists(VIEW))
                .andExpect(content().string(containsString("John Doe")));
    }

    // ========== GET /stays ==========

    @Test
    void currentStays_shouldRender() throws Exception {
        var reservation = createReservation(ReservationStatus.CHECKED_IN);
        Page<Reservation> page = new PageImpl<>(
                List.of(reservation), PageRequest.of(0, 10), 1);
        when(reservationService.findCheckedInFiltered(any(), any())).thenReturn(page);

        mockMvc.perform(get("/receptionist/stays"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/stays"))
                .andExpect(model().attributeExists(VIEW))
                .andExpect(content().string(containsString("John Doe")));
    }

    // ========== GET /{id} (detail) ==========

    @Test
    void detail_shouldRender() throws Exception {
        var reservation = createReservation(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(stayingGuestService.getGuests(1L)).thenReturn(List.of());
        when(paymentService.getPaymentsByReservationId(1L)).thenReturn(List.of());
        when(roomAssignmentService.getAssignedRooms(1L)).thenReturn(List.of());
        when(roomAssignmentService.getAvailableRooms(1L)).thenReturn(List.of());

        mockMvc.perform(get("/receptionist/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/detail"))
                .andExpect(content().string(containsString("John Doe")))
                .andExpect(content().string(containsString("Confirmed")));
    }

    // ========== GET /create ==========

    @Test
    void create_shouldRender() throws Exception {
        mockMvc.perform(get("/receptionist/bookings/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/create"))
                .andExpect(model().attributeExists(FORM));
    }

    // ========== GET /create/details ==========

    @Test
    void createDetails_shouldRender() throws Exception {
        var roomType = createRoomType(1L, "Deluxe");
        var availability = new RoomTypeAvailability(roomType, 10, 5);
        when(roomAvailabilityService.getAvailability(any(), any()))
                .thenReturn(List.of(availability));

        mockMvc.perform(get("/receptionist/bookings/create/details")
                        .param("checkInAt", "2026-07-01")
                        .param("checkOutAt", "2026-07-05"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/details"))
                .andExpect(model().attributeExists(VIEW, FORM))
                .andExpect(content().string(containsString("Deluxe")));
    }

    @Test
    void createDetails_shouldRedirect_whenDatesMissing() throws Exception {
        mockMvc.perform(get("/receptionist/bookings/create/details"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/create"));
    }

    // ========== GET /{id}/manage ==========

    @Test
    void manage_shouldRender() throws Exception {
        var reservation = createReservation(ReservationStatus.CHECKED_IN);
        mockManageView(reservation);

        mockMvc.perform(get("/receptionist/stays/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/manage"))
                .andExpect(model().attributeExists(VIEW, FORM))
                .andExpect(content().string(containsString("John Doe")));
    }

    @Test
    void manage_shouldRedirect_whenNotCheckedIn() throws Exception {
        var reservation = createReservation(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(get("/receptionist/stays/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays"));
    }

    // ========== POST /create (searchDates) ==========

    @Test
    void searchDates_shouldRedirectToDetails_whenValid() throws Exception {
        doNothing().when(dateSearchValidator).validate(any(), any());

        mockMvc.perform(post("/receptionist/bookings/create")
                        .param("checkInAt", "2026-07-01")
                        .param("checkOutAt", "2026-07-05"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/receptionist/bookings/create/details?checkInAt=2026-07-01&checkOutAt=2026-07-05"));
    }

    @Test
    void searchDates_shouldReRender_whenValidationFails() throws Exception {
        mockMvc.perform(post("/receptionist/bookings/create")
                        .param("checkInAt", "")
                        .param("checkOutAt", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/create"));
    }

    // ========== POST /create/details (createBooking) ==========

    @Test
    void createBooking_shouldRedirect_whenValid() throws Exception {
        doNothing().when(offlineBookingFormValidator).validate(any(), any());
        var saved = new Reservation();
        saved.setId(100L);
        when(reservationService.createReservation(any())).thenReturn(saved);

        mockMvc.perform(post("/receptionist/bookings/create/details")
                        .param("checkInAt", "2026-07-01")
                        .param("checkOutAt", "2026-07-05")
                        .param("guestName", "John Doe")
                        .param("guestEmail", "john@test.com")
                        .param("guestPhone", "1234567890")
                        .param("guestDateOfBirth", "1990-01-01")
                        .param("guestNationality", "US"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/100"))
                .andExpect(flash().attributeExists(SUCCESS));
    }

    @Test
    void createBooking_shouldReRender_whenValidationFails() throws Exception {
        var roomType = createRoomType(1L, "Deluxe");
        var availability = new RoomTypeAvailability(roomType, 10, 5);
        when(roomAvailabilityService.getAvailability(any(), any()))
                .thenReturn(List.of(availability));

        mockMvc.perform(post("/receptionist/bookings/create/details")
                        .param("checkInAt", "2026-07-01")
                        .param("checkOutAt", "2026-07-05")
                        .param("guestName", "")
                        .param("guestEmail", "")
                        .param("guestPhone", "")
                        .param("guestDateOfBirth", "")
                        .param("guestNationality", "")
                        .param("roomSelections[0].roomTypeId", "1")
                        .param("roomSelections[0].roomCount", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/details"));
    }

    // ========== POST /{id}/manage/guests (addGuest) ==========

    @Test
    void addGuest_shouldRedirect_whenValid() throws Exception {
        when(stayingGuestService.addGuest(anyLong(), any())).thenReturn(null);

        mockMvc.perform(post("/receptionist/stays/1/manage/guests")
                        .param("guestName", "Jane Doe")
                        .param("dateOfBirth", "1990-01-01")
                        .param("nationality", "US"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays/1"))
                .andExpect(flash().attributeExists(SUCCESS));

        verify(stayingGuestService).addGuest(eq(1L), any(StayingGuestForm.class));
    }

    @Test
    void addGuest_shouldReRender_whenValidationFails() throws Exception {
        var reservation = createReservation(ReservationStatus.CHECKED_IN);
        mockManageView(reservation);

        mockMvc.perform(post("/receptionist/stays/1/manage/guests")
                        .param("guestName", "")
                        .param("dateOfBirth", "")
                        .param("nationality", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/manage"));
    }

    // ========== POST /{id}/manage/guests/{guestId} (editGuest) ==========

    @Test
    void editGuest_shouldRedirect_whenValid() throws Exception {
        mockMvc.perform(post("/receptionist/stays/1/manage/guests/5")
                        .param("guestName", "Jane Doe")
                        .param("dateOfBirth", "1990-01-01")
                        .param("nationality", "US"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays/1"))
                .andExpect(flash().attributeExists(SUCCESS));

        verify(stayingGuestService).updateGuest(eq(1L), eq(5L), any(StayingGuestForm.class));
    }

    @Test
    void editGuest_shouldReRender_whenValidationFails() throws Exception {
        var reservation = createReservation(ReservationStatus.CHECKED_IN);
        mockManageView(reservation);

        mockMvc.perform(post("/receptionist/stays/1/manage/guests/5")
                        .param("guestName", "")
                        .param("dateOfBirth", "")
                        .param("nationality", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/manage"));
    }

    // ========== POST /{id}/manage/guests/{guestId}/delete (deleteGuest) ==========

    @Test
    void deleteGuest_shouldRedirect_whenValid() throws Exception {
        doNothing().when(stayingGuestService).deleteGuest(1L, 5L);

        mockMvc.perform(post("/receptionist/stays/1/manage/guests/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays/1"))
                .andExpect(flash().attributeExists(SUCCESS));

        verify(stayingGuestService).deleteGuest(1L, 5L);
    }

    // ========== POST /{id}/manage/rooms (assignRoom) ==========

    @Test
    void assignRoom_shouldRedirect_whenValid() throws Exception {
        when(roomAssignmentService.assignRoom(1L, 1L)).thenReturn(null);

        mockMvc.perform(post("/receptionist/stays/1/manage/rooms")
                        .param("roomId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays/1"))
                .andExpect(flash().attributeExists(SUCCESS));
    }

    @Test
    void assignRoom_shouldReRender_whenValidationFails() throws Exception {
        var reservation = createReservation(ReservationStatus.CHECKED_IN);
        mockManageView(reservation);

        mockMvc.perform(post("/receptionist/stays/1/manage/rooms"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/manage"));
    }

    @Test
    void assignRoom_shouldRedirectWithError_whenBusinessRuleFails() throws Exception {
        when(roomAssignmentService.assignRoom(anyLong(), anyLong()))
                .thenThrow(new IllegalStateException("Room is not vacant"));

        mockMvc.perform(post("/receptionist/stays/1/manage/rooms")
                        .param("roomId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays/1"))
                .andExpect(flash().attributeExists(ERROR));
    }

    // ========== POST /{id}/manage/rooms/{assignmentId}/remove (removeRoom) ==========

    @Test
    void removeRoom_shouldRedirect_whenValid() throws Exception {
        doNothing().when(roomAssignmentService).removeAssignment(1L, 2L);

        mockMvc.perform(post("/receptionist/stays/1/manage/rooms/2/remove"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays/1"))
                .andExpect(flash().attributeExists(SUCCESS));

        verify(roomAssignmentService).removeAssignment(1L, 2L);
    }

    // ========== POST /{id}/manage/extend (extendStay) ==========

    @Test
    void extendStay_shouldRedirect_whenValid() throws Exception {
        when(reservationService.extendStay(1L, 2)).thenReturn(new Reservation());

        mockMvc.perform(post("/receptionist/stays/1/manage/extend")
                        .param("extraDays", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays/1"))
                .andExpect(flash().attributeExists(SUCCESS));
    }

    @Test
    void extendStay_shouldReRender_whenValidationFails() throws Exception {
        var reservation = createReservation(ReservationStatus.CHECKED_IN);
        mockManageView(reservation);

        mockMvc.perform(post("/receptionist/stays/1/manage/extend"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/manage"));
    }

    @Test
    void extendStay_shouldRedirectWithError_whenBusinessRuleFails() throws Exception {
        when(reservationService.extendStay(anyLong(), anyInt()))
                .thenThrow(new IllegalStateException("Only checked-in reservations can be extended"));

        mockMvc.perform(post("/receptionist/stays/1/manage/extend")
                        .param("extraDays", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays/1"))
                .andExpect(flash().attributeExists(ERROR));
    }

    // ========== POST /{id}/cancel ==========

    @Test
    void cancel_shouldRedirectWithSuccess_whenReservationPending() throws Exception {
        doNothing().when(reservationService).cancelReservation(1L);

        mockMvc.perform(post("/receptionist/bookings/1/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(SUCCESS, "Reservation cancelled."));
    }

    @Test
    void cancel_shouldRedirectWithError_whenReservationCheckedIn() throws Exception {
        doThrow(new IllegalStateException("Only PENDING or CONFIRMED reservations can be cancelled"))
                .when(reservationService).cancelReservation(1L);

        mockMvc.perform(post("/receptionist/bookings/1/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(ERROR, "Only PENDING or CONFIRMED reservations can be cancelled"));
    }

    // ========== POST /{id}/refund ==========

    private Payment createPayPalPayment(Long id, String orderId, BigDecimal amount) {
        Payment p = new Payment();
        p.setId(id);
        p.setPaymentMethod(PaymentMethod.PAYPAL);
        p.setOrderId(orderId);
        p.setAmount(amount);
        p.setConfirmedAt(java.time.LocalDateTime.now());
        return p;
    }

    private Payment createCashPayment(Long id, BigDecimal amount) {
        Payment p = new Payment();
        p.setId(id);
        p.setPaymentMethod(PaymentMethod.CASH);
        p.setAmount(amount);
        p.setConfirmedAt(java.time.LocalDateTime.now());
        return p;
    }

    @Test
    void refund_shouldRedirectWithSuccess_whenPayPalRefundSucceeds() throws Exception {
        var reservation = createReservation(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        var payments = List.of(createPayPalPayment(1L, "ORDER123", BigDecimal.valueOf(200)));
        when(paymentService.getPaymentsByReservationId(1L)).thenReturn(payments);
        when(paymentService.refundOnlineBookingPayment("ORDER123")).thenReturn(true);
        when(paymentService.calculateRefund(reservation)).thenReturn(BigDecimal.valueOf(50));

        mockMvc.perform(post("/receptionist/bookings/1/refund"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(SUCCESS, "Refund processed: $50"));

        verify(paymentService).refundOnlineBookingPayment("ORDER123");
        verify(paymentService).markRefunded(reservation, payments);
    }

    @Test
    void refund_shouldRedirectWithError_whenPayPalRefundFails() throws Exception {
        var reservation = createReservation(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        var payments = List.of(createPayPalPayment(1L, "ORDER123", BigDecimal.valueOf(200)));
        when(paymentService.getPaymentsByReservationId(1L)).thenReturn(payments);
        when(paymentService.refundOnlineBookingPayment("ORDER123")).thenReturn(false);

        mockMvc.perform(post("/receptionist/bookings/1/refund"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(ERROR, "PayPal refund failed for order ORDER123"));

        verify(paymentService, never()).markRefunded(any(), any());
    }

    @Test
    void refund_shouldRedirectWithError_whenReservationNotConfirmed() throws Exception {
        var reservation = createReservation(ReservationStatus.PENDING);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(post("/receptionist/bookings/1/refund"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(ERROR, "Only confirmed reservations can be refunded"));

        verify(paymentService, never()).getPaymentsByReservationId(anyLong());
    }

    @Test
    void refund_shouldRedirectWithError_whenNoPaymentsFound() throws Exception {
        var reservation = createReservation(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(paymentService.getPaymentsByReservationId(1L)).thenReturn(List.of());

        mockMvc.perform(post("/receptionist/bookings/1/refund"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(ERROR, "No payments found for this reservation"));
    }

    // ========== POST /{id}/refund/offline ==========

    @Test
    void refundOffline_shouldRedirectWithSuccess_whenConfirmed() throws Exception {
        var reservation = createReservation(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        var payments = List.of(createCashPayment(1L, BigDecimal.valueOf(200)));
        when(paymentService.getPaymentsByReservationId(1L)).thenReturn(payments);
        when(paymentService.calculateRefund(reservation)).thenReturn(BigDecimal.valueOf(50));

        mockMvc.perform(post("/receptionist/bookings/1/refund/offline"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(SUCCESS, "Offline refund processed: $50"));

        verify(paymentService).markRefunded(reservation, payments);
    }

    @Test
    void refundOffline_shouldRedirectWithError_whenNotConfirmed() throws Exception {
        var reservation = createReservation(ReservationStatus.PENDING);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(post("/receptionist/bookings/1/refund/offline"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(ERROR, "Only confirmed reservations can be refunded offline"));
    }

    // ========== POST /{id}/guests/add (BookingController) ==========

    @Test
    void addGuest_shouldRedirectWithSuccess_whenValid() throws Exception {
        when(stayingGuestService.addGuest(anyLong(), any())).thenReturn(new StayingGuest());

        mockMvc.perform(post("/receptionist/bookings/1/guests/add")
                        .param("guestName", "Jane Doe")
                        .param("dateOfBirth", "1990-01-01")
                        .param("nationality", "US"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(SUCCESS, "Guest added successfully."));

        verify(stayingGuestService).addGuest(eq(1L), any(StayingGuestForm.class));
    }

    // ========== POST /{id}/rooms/assign (BookingController) ==========

    @Test
    void assignRoom_shouldRedirectWithSuccess_whenRoomVacant() throws Exception {
        when(roomAssignmentService.assignRoom(1L, 5L)).thenReturn(new RoomAssignment());

        mockMvc.perform(post("/receptionist/bookings/1/rooms/assign")
                        .param("roomId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attribute(SUCCESS, "Room assigned successfully."));

        verify(roomAssignmentService).assignRoom(1L, 5L);
    }
}
