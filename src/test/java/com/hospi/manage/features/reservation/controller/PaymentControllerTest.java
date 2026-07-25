package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.payment.enums.PaymentMethod;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
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

import java.time.LocalDate;

import static com.hospi.manage.common.constant.Attributes.*;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AuditService auditService;

    private Reservation createReservation(ReservationStatus status) {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setGuestName("John Doe");
        r.setGuestEmail("john@test.com");
        r.setCheckInAt(LocalDate.of(2026, 7, 1));
        r.setCheckOutAt(LocalDate.of(2026, 7, 5));
        r.setStatus(status);
        return r;
    }

    @BeforeEach
    void setUpSecurityContext() {
        Account account = new Account();
        account.setEmail("receptionist@test.com");
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

    // --- GET /{id}/payment ---

    @Test
    void paymentForm_shouldRender() throws Exception {
        var reservation = createReservation(ReservationStatus.PENDING);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(get("/receptionist/reservations/1/payment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"));
    }

    @Test
    void paymentForm_shouldRedirect_whenNotPending() throws Exception {
        var reservation = createReservation(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(get("/receptionist/reservations/1/payment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"));
    }

    // --- POST /{id}/payment ---

    @Test
    void confirmPayment_shouldRedirectWithSuccess() throws Exception {
        mockMvc.perform(post("/receptionist/reservations/1/payment")
                        .param("paymentMethod", "CASH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attributeExists(SUCCESS));

        verify(paymentService).confirmPayment(eq(1L), eq(PaymentMethod.CASH), eq("receptionist@test.com"));
    }

    @Test
    void confirmPayment_shouldRedirectWithError_whenBusinessRuleFails() throws Exception {
        doThrow(new IllegalStateException("Booking is not in PENDING status"))
                .when(paymentService).confirmPayment(eq(1L), any(), any());

        mockMvc.perform(post("/receptionist/reservations/1/payment")
                        .param("paymentMethod", "CARD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/bookings/1"))
                .andExpect(flash().attributeExists(ERROR));
    }
}

