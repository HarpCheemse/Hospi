package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.features.invoice.service.RevenueService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.reservation.service.ReservationService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagerDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class ManagerDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private RevenueService revenueService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void dashboard_shouldRender() throws Exception {
        when(reservationService.findByStatus(any())).thenReturn(List.of());
        when(reservationService.findByStatuses(any())).thenReturn(List.of());
        when(roomService.findAll()).thenReturn(List.of());
        when(revenueService.getRevenueView(any(), any(), any())).thenReturn(
                new com.hospi.manage.features.invoice.dto.response.RevenueView(
                        java.math.BigDecimal.ZERO, 0, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO, 0, java.math.BigDecimal.ZERO,
                        List.of(), List.of(), List.of(), List.of(), List.of(),
                        java.time.LocalDate.now(), java.time.LocalDate.now(), "this-month"));

        mockMvc.perform(get("/manager"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/dashboard"))
                .andExpect(model().attributeExists("checkInsToday"))
                .andExpect(content().string(containsString("Manager Dashboard")));
    }
}
