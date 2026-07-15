package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAssignmentService;
import com.hospi.manage.features.room.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Unit tests for the ReceptionistDashboardController. */
@WebMvcTest(ReceptionistDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceptionistDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private RoomAssignmentService roomAssignmentService;

    @MockitoBean
    private com.hospi.manage.features.notification.service.NotificationService notificationService;

    @Test
    void dashboard_shouldRender() throws Exception {
        when(reservationService.findByStatus(ReservationStatus.CONFIRMED)).thenReturn(List.of());
        when(reservationService.findByStatus(ReservationStatus.CHECKED_IN)).thenReturn(List.of());
        when(reservationService.findByStatusAndCheckOutAt(eq(ReservationStatus.CHECKED_OUT), any())).thenReturn(List.of());
        when(roomService.findAll()).thenReturn(List.of());
        when(roomAssignmentService.findAssignmentsByReservationIds(any())).thenReturn(List.of());
        when(reservationService.findPaymentsByReservationIds(any())).thenReturn(List.of());

        mockMvc.perform(get("/receptionist"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/dashboard"))
                .andExpect(content().string(containsString("Receptionist Dashboard")));
    }
}
