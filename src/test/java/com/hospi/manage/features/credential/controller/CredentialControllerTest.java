package com.hospi.manage.features.credential.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.validator.AccountValidator;
import com.hospi.manage.features.credential.dto.ChangePasswordForm;
import com.hospi.manage.features.credential.dto.CredentialView;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.credential.service.CredentialService;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CredentialController.class)
@AutoConfigureMockMvc(addFilters = false)
class CredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CredentialService credentialService;

    @MockitoBean
    private AccountValidator accountValidator;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AuditService auditService;

    @BeforeEach
    void setUpSecurityContext() {
        var account = new Account();
        account.setId(1L);
        account.setEmail("receptionist@test.com");
        account.setRole(Role.RECEPTIONIST);
        account.setActive(true);
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
    void credentials_shouldRender() throws Exception {
        var view = new CredentialView("Receptionist User", "receptionist@test.com",
                "RECEPTIONIST", "1234567890", "ACTIVE", LocalDateTime.now());
        when(credentialService.getCredentialView(1L)).thenReturn(view);

        mockMvc.perform(get("/credentials"))
                .andExpect(status().isOk())
                .andExpect(view().name("receptionist/credential"))
                .andExpect(model().attributeExists("view"))
                .andExpect(model().attribute("activeSidebar", "CREDENTIALS"))
                .andExpect(content().string(containsString("Receptionist User")));
    }

    @Test
    void changePasswordPage_shouldRender() throws Exception {
        mockMvc.perform(get("/credentials/change-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-password"))
                .andExpect(model().attributeExists(Attributes.FORM));
    }

    @Test
    void changePassword_shouldRedirect_onSuccess() throws Exception {
        mockMvc.perform(post("/credentials/change-password")
                        .param("currentPassword", "oldPass")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/credentials"));

        verify(credentialService).changePassword(any(), any());
    }

    @Test
    void changePassword_shouldReRender_onValidationError() throws Exception {
        doAnswer(invocation -> {
            BindingResult br = invocation.getArgument(2);
            br.rejectValue("currentPassword", "invalid", "Current password is incorrect");
            return null;
        }).when(accountValidator).validateChangePassword(any(), any(), any(), any());

        mockMvc.perform(post("/credentials/change-password")
                        .param("currentPassword", "wrong")
                        .param("newPassword", "newPass")
                        .param("confirmPassword", "newPass"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/change-password"));
    }
}

