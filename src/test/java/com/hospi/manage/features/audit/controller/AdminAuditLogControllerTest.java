package com.hospi.manage.features.audit.controller;

import com.hospi.manage.features.audit.repository.AuditLogRepository;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void list_shouldRender() throws Exception {
        when(auditLogRepository.findFiltered(any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit-log/list"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(content().string(containsString("Audit Logs")));
    }

    @Test
    void list_shouldRender_withActionFilter() throws Exception {
        when(auditLogRepository.findFiltered(any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/admin/audit-logs?action=CREATE"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit-log/list"))
                .andExpect(model().attribute("filterAction", "CREATE"));

        verify(auditLogRepository).findFiltered(eq("CREATE"), eq(null), eq(null), eq(null), any(PageRequest.class));
    }

    @Test
    void list_shouldRender_withStaffNameFilter() throws Exception {
        when(auditLogRepository.findFiltered(any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/admin/audit-logs?staffName=Alice"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit-log/list"))
                .andExpect(model().attribute("filterStaff", "Alice"));

        verify(auditLogRepository).findFiltered(eq(null), eq("alice"), eq(null), eq(null), any(PageRequest.class));
    }

    @Test
    void list_shouldRender_withDateRangeFilter() throws Exception {
        when(auditLogRepository.findFiltered(any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/admin/audit-logs?startDate=2026-01-01T00:00&endDate=2026-12-31T23:59"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit-log/list"))
                .andExpect(model().attributeExists("filterStart"))
                .andExpect(model().attributeExists("filterEnd"));
    }

    @Test
    void list_shouldRender_withPagination() throws Exception {
        when(auditLogRepository.findFiltered(any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/admin/audit-logs?page=2"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit-log/list"));

        verify(auditLogRepository).findFiltered(eq(null), eq(null), eq(null), eq(null), eq(PageRequest.of(2, 25)));
    }
}
