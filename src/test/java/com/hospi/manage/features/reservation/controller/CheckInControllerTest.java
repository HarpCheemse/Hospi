package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.AccountStatus;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.notification.service.NotificationService;
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

@WebMvcTest(CheckInController.class)
@AutoConfigureMockMvc(addFilters = false)
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private NotificationService notificationService;

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
        account.setStatus(AccountStatus.ACTIVE);
        AccountPrincipal principal = new AccountPrincipal(account);
        var auth = new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(), principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // --- GET /{id}/checkin ---

    @Test
    void checkInForm_shouldRender() throws Exception {
        var reservation = createReservation(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(get("/receptionist/reservations/1/checkin"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/checkin"))
                .andExpect(model().attributeExists(VIEW, FORM))
                .andExpect(content().string(containsString("John Doe")));
    }

    @Test
    void checkInForm_shouldRedirect_whenNotConfirmed() throws Exception {
        var reservation = createReservation(ReservationStatus.CHECKED_IN);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(get("/receptionist/reservations/1/checkin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/reservations"));
    }

    // --- POST /{id}/checkin ---

    @Test
    void confirmCheckIn_shouldRedirectWithSuccess() throws Exception {
        mockMvc.perform(post("/receptionist/reservations/1/checkin")
                        .param("bookingCode", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/reservations"))
                .andExpect(flash().attributeExists(SUCCESS));

        verify(reservationService).checkIn(eq(1L), any(LocalDate.class), eq(""), eq("receptionist@test.com"));
    }

    @Test
    void confirmCheckIn_shouldRedirectWithError_whenBusinessRuleFails() throws Exception {
        doThrow(new IllegalStateException("Only confirmed bookings can be checked in"))
                .when(reservationService).checkIn(eq(1L), any(LocalDate.class), any(), any());

        mockMvc.perform(post("/receptionist/reservations/1/checkin")
                        .param("bookingCode", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/reservations"))
                .andExpect(flash().attributeExists(ERROR));
    }
}
