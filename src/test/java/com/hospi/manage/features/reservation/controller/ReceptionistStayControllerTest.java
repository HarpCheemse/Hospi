package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.reservation.dto.request.CheckoutForm;
import com.hospi.manage.features.reservation.dto.response.CheckoutView;
import com.hospi.manage.features.reservation.dto.response.ReservationSummaryView;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.*;
import com.hospi.manage.features.config.service.SystemConfigService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceptionistStayController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceptionistStayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ReservationService reservationService;
    @MockitoBean private StayingGuestService stayingGuestService;
    @MockitoBean private RoomAssignmentService roomAssignmentService;
    @MockitoBean private RoomUpgradeService roomUpgradeService;
    @MockitoBean private CheckoutService checkoutService;
    @MockitoBean private SystemConfigService systemConfigService;
    @MockitoBean private AuditService auditService;
    @MockitoBean private NotificationService notificationService;

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

    private Reservation createCheckedInReservation() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setStatus(ReservationStatus.CHECKED_IN);
        r.setGuestName("John Smith");
        r.setGuestEmail("john@hospi.com");
        r.setCheckInAt(LocalDate.of(2026, 6, 20));
        r.setCheckOutAt(LocalDate.of(2026, 6, 26));
        r.setSource(BookingSource.OFFLINE);
        r.setTotalPrice(BigDecimal.valueOf(500));
        r.setDetails(List.of());
        return r;
    }

    private ReservationSummaryView createSummaryView() {
        return new ReservationSummaryView(
                1L, "John Smith", "john@hospi.com", null, null, null,
                LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 26),
                ReservationStatus.CHECKED_IN, BookingSource.OFFLINE, null,
                BigDecimal.valueOf(500), null, null, BigDecimal.ZERO, BigDecimal.ZERO,
                List.of());
    }

    private CheckoutView createCheckoutView() {
        return new CheckoutView(
                createSummaryView(),
                BigDecimal.valueOf(500), BigDecimal.valueOf(40),
                BigDecimal.valueOf(540), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, false, BigDecimal.valueOf(540),
                List.of(PaymentMethod.CASH, PaymentMethod.CARD), "12:00", "");
    }

    @Test
    void showCheckout_shouldRender() throws Exception {
        Reservation r = createCheckedInReservation();
        when(reservationService.findById(1L)).thenReturn(r);
        when(checkoutService.buildCheckoutView(1L)).thenReturn(createCheckoutView());

        mockMvc.perform(get("/receptionist/stays/1/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/checkout"))
                .andExpect(model().attributeExists("view"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void showCheckout_shouldRedirect_whenNotCheckedIn() throws Exception {
        Reservation r = createCheckedInReservation();
        r.setStatus(ReservationStatus.PENDING);
        when(reservationService.findById(1L)).thenReturn(r);

        mockMvc.perform(get("/receptionist/stays/1/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays"));
    }

    @Test
    void completeCheckout_shouldRedirect_onSuccess() throws Exception {
        Reservation r = createCheckedInReservation();
        when(reservationService.findById(1L)).thenReturn(r);
        when(checkoutService.buildCheckoutView(1L)).thenReturn(createCheckoutView());

        mockMvc.perform(post("/receptionist/stays/1/checkout")
                        .param("paymentMethod", "CASH")
                        .param("applyLateCheckoutFee", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/receipts/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void completeCheckout_shouldReRender_whenValidationFails() throws Exception {
        when(checkoutService.buildCheckoutView(1L)).thenReturn(createCheckoutView());

        mockMvc.perform(post("/receptionist/stays/1/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/checkout"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void completeCheckout_shouldRedirect_onBusinessError() throws Exception {
        doThrow(new IllegalStateException("Already checked out"))
                .when(checkoutService).complete(anyLong(), any(), anyString());

        mockMvc.perform(post("/receptionist/stays/1/checkout")
                        .param("paymentMethod", "CASH")
                        .param("applyLateCheckoutFee", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/stays/1/checkout"))
                .andExpect(flash().attributeExists("error"));
    }
}
