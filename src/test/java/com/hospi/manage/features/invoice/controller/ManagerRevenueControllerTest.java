package com.hospi.manage.features.invoice.controller;

import com.hospi.manage.features.invoice.dto.response.RevenueChartPoint;
import com.hospi.manage.features.invoice.dto.response.RevenueView;
import com.hospi.manage.features.invoice.service.RevenueService;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagerRevenueController.class)
@AutoConfigureMockMvc(addFilters = false)
class ManagerRevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevenueService revenueService;

    @MockitoBean
    private NotificationService notificationService;

    private RevenueView aRevenueView() {
        int year = LocalDate.now().getYear();
        return new RevenueView(
                BigDecimal.valueOf(50000),
                25,
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(5000),
                120,
                BigDecimal.valueOf(45000),
                List.of(new RevenueChartPoint("Jan " + year, BigDecimal.valueOf(50000), 25)),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31),
                String.valueOf(year)
        );
    }

    @Test
    void revenue_shouldRender_whenDefaultYear() throws Exception {
        when(revenueService.getRevenueView(any(), any(), any())).thenReturn(aRevenueView());

        mockMvc.perform(get("/manager/revenues").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/revenues"))
                .andExpect(model().attributeExists("view"))
                .andExpect(content().string(containsString("Revenue Reports")));
    }

    @Test
    void revenue_shouldRender_whenSpecificYear() throws Exception {
        when(revenueService.getRevenueView(any(), any(), any())).thenReturn(aRevenueView());

        mockMvc.perform(get("/manager/revenues?year=2025").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/revenues"))
                .andExpect(model().attribute("selectedYear", 2025))
                .andExpect(content().string(containsString("Revenue Reports")));
    }

    @Test
    void revenue_shouldRender_whenNoData() throws Exception {
        int year = LocalDate.now().getYear();
        RevenueView empty = new RevenueView(
                BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                0, BigDecimal.ZERO, List.of(), List.of(), List.of(), List.of(), List.of(),
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), String.valueOf(year));
        when(revenueService.getRevenueView(any(), any(), any())).thenReturn(empty);

        mockMvc.perform(get("/manager/revenues").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/revenues"))
                .andExpect(content().string(containsString("No Revenue Data")));
    }
}
