package com.hospi.manage.features.notification.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.admin.account.entity.Account;
import com.hospi.manage.features.admin.account.enums.AccountStatus;
import com.hospi.manage.features.admin.account.enums.Role;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    void setUpSecurityContext() {
        var account = new Account();
        account.setId(1L);
        account.setEmail("receptionist@test.com");
        account.setRole(Role.RECEPTIONIST);
        account.setStatus(AccountStatus.ACTIVE);
        var principal = new AccountPrincipal(account);
        var auth = new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(), principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void notifications_shouldRender() throws Exception {
        when(notificationService.getNotifications(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/notifications"))
                .andExpect(model().attributeExists("notifications"));
    }

    @Test
    void notifications_shouldRenderForAdmin() throws Exception {
        var adminAccount = new Account();
        adminAccount.setId(1L);
        adminAccount.setEmail("admin@test.com");
        adminAccount.setRole(Role.ADMIN);
        adminAccount.setStatus(AccountStatus.ACTIVE);
        var adminPrincipal = new AccountPrincipal(adminAccount);
        var auth = new UsernamePasswordAuthenticationToken(
                adminPrincipal, adminPrincipal.getPassword(), adminPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(notificationService.getNotifications(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notifications"))
                .andExpect(model().attributeExists("notifications"));

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    void markAllRead_shouldRedirect() throws Exception {
        mockMvc.perform(post("/notifications/mark-all-read"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"))
                .andExpect(flash().attributeExists(Attributes.SUCCESS));
    }
}
