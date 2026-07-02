package com.hospi.manage.features.reservation.controller;

import com.hospi.manage.features.config.entity.SystemConfig;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.payment.entity.Payment;
import com.hospi.manage.features.payment.repository.PaymentRepository;
import com.hospi.manage.features.receptionist.controller.ReceptionistCheckoutController;
import com.hospi.manage.features.receptionist.dto.CheckoutCalculation;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.CheckoutService;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.StayingGuestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceptionistCheckoutController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceptionistCheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private CheckoutService checkoutService;

    @MockitoBean
    private StayingGuestService stayingGuestService;

    @MockitoBean
    private SystemConfigService systemConfigService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private PaymentRepository paymentRepository;

    private Reservation createReservation(ReservationStatus status) {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(status);
        reservation.setGuestName("John Smith");
        reservation.setGuestEmail("john@hospi.com");
        reservation.setGuestPhone("+84 912 345 678");
        reservation.setCheckInAt(LocalDate.of(2026,
                6,
                20));
        reservation.setCheckOutAt(LocalDate.of(2026,
                6,
                26));
        reservation.setSource(BookingSource.ONLINE);
        reservation.setTotalPrice(BigDecimal.valueOf(500));
        reservation.setCheckedOutAt(LocalDateTime.of(2026,
                6,
                26,
                11,
                0));
        reservation.setCheckedOutBy("receptionist");
        reservation.setLateCheckoutFeeApplied(null);
        reservation.setExtraGuestFeeApplied(null);
        reservation.setDetails(new ArrayList<>());
        return reservation;
    }

    @Test
    void checkout_shouldRender() throws Exception {
        Reservation reservation = createReservation(ReservationStatus.CHECKED_IN);
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(stayingGuestService.getGuests(1L)).thenReturn(List.of(new StayingGuest(),
                new StayingGuest()));
        when(checkoutService.calculate(any(),
                any(),
                anyInt())).thenReturn(
                new CheckoutCalculation(BigDecimal.valueOf(500),
                        BigDecimal.valueOf(200),
                        BigDecimal.valueOf(300),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(300),
                        false,
                        0));
        SystemConfig config = new SystemConfig();
        config.setLateCheckoutFee(BigDecimal.valueOf(50));
        config.setExtraGuestFee(BigDecimal.valueOf(25));
        when(systemConfigService.getConfig()).thenReturn(config);

        mockMvc.perform(get("/receptionist/reservations/1/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/checkout"))
                .andExpect(model().attributeExists("reservation"))
                .andExpect(model().attributeExists("calc"))
                .andExpect(model().attributeExists("config"))
                .andExpect(model().attributeExists("registeredGuests"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void checkout_shouldRedirect_whenWrongStatus() throws Exception {
        Reservation reservation = createReservation(ReservationStatus.PENDING);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(get("/receptionist/reservations/1/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/reservations"));
    }

    @Test
    void completeCheckout_shouldRedirect_onSuccess() throws Exception {
        Reservation reservation = createReservation(ReservationStatus.CHECKED_IN);
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(stayingGuestService.getGuests(1L)).thenReturn(List.of(new StayingGuest(),
                new StayingGuest()));
        when(checkoutService.calculate(any(),
                any(),
                anyInt())).thenReturn(
                new CheckoutCalculation(BigDecimal.valueOf(500),
                        BigDecimal.valueOf(200),
                        BigDecimal.valueOf(300),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(300),
                        false,
                        0));

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("receptionist");

        mockMvc.perform(post("/receptionist/reservations/1/checkout")
                        .param("paymentMethod",
                                "CASH")
                        .param("amountReceived",
                                "300")
                        .param("actualCheckoutTime",
                                "2026-06-26T11:00")
                        .param("applyLateFee",
                                "false")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/reservations/1/receipt"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void completeCheckout_shouldRedirect_onError() throws Exception {
        Reservation reservation = createReservation(ReservationStatus.CHECKED_IN);
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(stayingGuestService.getGuests(1L)).thenReturn(List.of(new StayingGuest(),
                new StayingGuest()));
        when(checkoutService.calculate(any(),
                any(),
                anyInt())).thenReturn(
                new CheckoutCalculation(BigDecimal.valueOf(500),
                        BigDecimal.valueOf(200),
                        BigDecimal.valueOf(300),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(300),
                        false,
                        0));
        doThrow(new IllegalStateException("Payment declined"))
                .when(checkoutService).complete(anyLong(),
                        any(),
                        any(),
                        any(),
                        anyString(),
                        any(),
                        anyBoolean(),
                        any());

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("receptionist");

        mockMvc.perform(post("/receptionist/reservations/1/checkout")
                        .param("paymentMethod",
                                "CASH")
                        .param("amountReceived",
                                "300")
                        .param("actualCheckoutTime",
                                "2026-06-26T11:00")
                        .param("applyLateFee",
                                "false")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/reservations/1/checkout"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void receipt_shouldRender() throws Exception {
        Reservation reservation = createReservation(ReservationStatus.CHECKED_OUT);
        when(reservationService.findById(1L)).thenReturn(reservation);

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(200));
        when(paymentRepository.findAllByReservationId(1L)).thenReturn(List.of(payment));

        mockMvc.perform(get("/receptionist/reservations/1/receipt"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/reservation/receipt"))
                .andExpect(model().attributeExists("reservation"))
                .andExpect(model().attributeExists("depositPaid"))
                .andExpect(model().attributeExists("totalPaid"));
    }

    @Test
    void receipt_shouldRedirect_whenWrongStatus() throws Exception {
        Reservation reservation = createReservation(ReservationStatus.CHECKED_IN);
        when(reservationService.findById(1L)).thenReturn(reservation);

        mockMvc.perform(get("/receptionist/reservations/1/receipt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receptionist/reservations"));
    }
}
