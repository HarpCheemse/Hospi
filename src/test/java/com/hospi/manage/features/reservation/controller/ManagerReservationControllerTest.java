package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReceiptService;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagerReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ManagerReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private StayingGuestService stayingGuestService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private ReceiptService receiptService;

    private Reservation aReservation(Long id) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setGuestName("John Doe");
        r.setGuestEmail("john@email.com");
        r.setTotalPrice(BigDecimal.valueOf(500));
        r.setStatus(ReservationStatus.CONFIRMED);
        r.setSource(BookingSource.OFFLINE);
        return r;
    }

    private void mockCounts() {
        when(reservationService.findByStatus(any())).thenReturn(List.of());
    }

    @Test
    void list_shouldRender_whenDefault() throws Exception {
        when(reservationService.findFiltered(any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(paymentService.getPaymentsByReservationId(anyList())).thenReturn(List.of());
        mockCounts();

        mockMvc.perform(get("/manager/reservations").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/reservation/list"))
                .andExpect(model().attributeExists("view"))
                .andExpect(content().string(containsString("Reservations")));
    }

    @Test
    void list_shouldRender_withScope() throws Exception {
        when(reservationService.findByStatuses(any())).thenReturn(List.of());
        when(paymentService.getPaymentsByReservationId(anyList())).thenReturn(List.of());
        mockCounts();

        mockMvc.perform(get("/manager/reservations?scope=today").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/reservation/list"))
                .andExpect(content().string(containsString("Reservations")));
    }

    @Test
    void list_shouldRender_withScopeAndStatus() throws Exception {
        when(reservationService.findByStatuses(any())).thenReturn(List.of());
        when(paymentService.getPaymentsByReservationId(anyList())).thenReturn(List.of());
        mockCounts();

        mockMvc.perform(get("/manager/reservations?scope=upcoming&status=PENDING").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/reservation/list"))
                .andExpect(model().attribute("activeScope", "upcoming"))
                .andExpect(model().attribute("activeStatus", "PENDING"))
                .andExpect(content().string(containsString("Reservations")));
    }

    @Test
    void list_shouldRender_withStatusOnly() throws Exception {
        when(reservationService.findFiltered(any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        mockCounts();

        mockMvc.perform(get("/manager/reservations?status=CHECKED_IN").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/reservation/list"))
                .andExpect(model().attribute("activeStatus", "CHECKED_IN"))
                .andExpect(content().string(containsString("Reservations")));
    }

    @Test
    void detail_shouldRender_whenReservationExists() throws Exception {
        var reservation = aReservation(1L);
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(stayingGuestService.getGuests(1L)).thenReturn(List.of());
        when(paymentService.getPaymentsByReservationId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/manager/reservations/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/reservation/detail"))
                .andExpect(model().attributeExists("reservation"))
                .andExpect(content().string(containsString("John Doe")));
    }

    @Test
    void detail_shouldRender_whenReservationNotFound() throws Exception {
        when(reservationService.findById(99L)).thenThrow(new ResourceNotFoundException("Reservation"));

        mockMvc.perform(get("/manager/reservations/99").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    @Test
    void refund_shouldRedirect_whenConfirmed() throws Exception {
        var reservation = aReservation(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        var payment = new Payment();
        payment.setId(1L);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setPaymentMethod(com.hospi.manage.features.payment.enums.PaymentMethod.CARD);
        when(paymentService.getPaymentsByReservationId(1L)).thenReturn(List.of(payment));
        when(paymentService.calculateRefund(reservation)).thenReturn(BigDecimal.valueOf(350));

        mockMvc.perform(post("/manager/reservations/1/refund").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/reservations/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void refundOffline_shouldRedirect_whenConfirmed() throws Exception {
        var reservation = aReservation(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        var payment = new Payment();
        payment.setId(1L);
        payment.setAmount(BigDecimal.valueOf(500));
        when(paymentService.getPaymentsByReservationId(1L)).thenReturn(List.of(payment));
        when(paymentService.calculateRefund(reservation)).thenReturn(BigDecimal.valueOf(350));

        mockMvc.perform(post("/manager/reservations/1/refund/offline").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/reservations/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void refund_shouldRedirectWithError_whenNotConfirmed() throws Exception {
        var reservation = aReservation(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(post("/manager/reservations/1/refund").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/reservations"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void cancel_shouldRedirect_whenValid() throws Exception {
        mockMvc.perform(post("/manager/reservations/1/cancel").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/reservations"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void cancel_shouldRedirectWithError_whenNotPending() throws Exception {
        doThrow(new IllegalStateException("Only PENDING reservations can be cancelled"))
                .when(reservationService).cancelReservation(1L);

        mockMvc.perform(post("/manager/reservations/1/cancel").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/reservations"))
                .andExpect(flash().attributeExists("error"));
    }
}
