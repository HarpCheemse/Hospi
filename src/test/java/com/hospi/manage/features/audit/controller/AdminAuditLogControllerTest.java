package com.hospi.manage.features.audit.controller;

import com.hospi.manage.features.audit.repository.AuditLogRepository;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
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
        when(auditLogRepository.findAllByOrderByCreatedAtDesc(any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit-log/list"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(content().string(containsString("Audit Logs")));
    }
}
