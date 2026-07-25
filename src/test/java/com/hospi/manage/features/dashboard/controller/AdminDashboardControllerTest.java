package com.hospi.manage.features.dashboard.controller;

import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.repository.AccountRepository;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.notification.service.NotificationService;
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

@WebMvcTest(AdminDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void dashboard_shouldRender() throws Exception {
        when(accountRepository.count()).thenReturn(10L);
        when(accountRepository.findByRole(any(Role.class))).thenReturn(List.of());
        when(auditService.getRecent()).thenReturn(List.of());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("totalStaff"))
                .andExpect(content().string(containsString("Admin Dashboard")));
    }
}
