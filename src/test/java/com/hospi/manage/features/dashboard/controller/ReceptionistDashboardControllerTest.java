package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceptionistDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceptionistDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void dashboard_shouldRender() throws Exception {
        mockMvc.perform(get("/receptionist"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/dashboard"))
                .andExpect(content().string(containsString("DASHBOARD")));
    }
}
