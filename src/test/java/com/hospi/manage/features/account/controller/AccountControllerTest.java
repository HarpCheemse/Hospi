package com.hospi.manage.features.account.controller;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.account.dto.AccountCreateForm;
import com.hospi.manage.features.account.dto.AccountEditForm;
import com.hospi.manage.features.account.dto.AccountView;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.service.AccountService;
import com.hospi.manage.features.account.validator.AccountValidator;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.core.security.session.AccountPrincipal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.util.List;

import static com.hospi.manage.common.constant.Attributes.ERROR;
import static com.hospi.manage.common.constant.Attributes.FORM;
import static com.hospi.manage.common.constant.Attributes.SUCCESS;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AccountValidator accountValidator;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AuditService auditService;

    @Test
    void list_shouldRender() throws Exception {
        var accounts = List.of(
                new AccountView(1L, "Alice Admin", "alice@test.com", "123", Role.ADMIN),
                new AccountView(2L, "Bob Receptionist", "bob@test.com", "456", Role.RECEPTIONIST)
        );
        when(accountService.findAllViews()).thenReturn(accounts);

        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/list"))
                .andExpect(model().attributeExists("view"))
                .andExpect(content().string(containsString("Staff Accounts")));
    }

    @Test
    void create_shouldRender() throws Exception {
        mockMvc.perform(get("/admin/accounts/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/create"))
                .andExpect(model().attributeExists("view"))
                .andExpect(model().attributeExists(FORM))
                .andExpect(content().string(containsString("Create Account")));
    }

    @Test
    void detail_shouldRender() throws Exception {
        var account = new AccountView(1L, "Test User", "test@test.com", "789", Role.MANAGER);
        when(accountService.getAccountView(1L)).thenReturn(account);

        mockMvc.perform(get("/admin/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/detail"))
                .andExpect(model().attributeExists("account"))
                .andExpect(content().string(containsString("Test User")));
    }

    @Test
    void edit_shouldRender() throws Exception {
        var editForm = new AccountEditForm("Test User", "test@test.com", "789", Role.MANAGER);
        when(accountService.getEditForm(1L)).thenReturn(editForm);

        mockMvc.perform(get("/admin/accounts/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/edit"))
                .andExpect(model().attributeExists(FORM))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("accountId"))
                .andExpect(content().string(containsString("Edit Account")))
                .andExpect(content().string(containsString("account information")));
    }

    @Test
    void createAccount_shouldRedirect_onSuccess() throws Exception {
        mockMvc.perform(post("/admin/accounts/create")
                        .param("fullName", "Test User")
                        .param("email", "test@test.com")
                        .param("phone", "1234567890")
                        .param("role", "RECEPTIONIST"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attributeExists(SUCCESS));

        verify(accountService).createAccount(any());
    }

    @Test
    void createAccount_shouldReRender_onValidationError() throws Exception {
        mockMvc.perform(post("/admin/accounts/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/create"))
                .andExpect(model().attributeExists("view"));
    }

    @Test
    void createAccount_shouldReRender_whenValidatorRejects() throws Exception {
        doAnswer(invocation -> {
            BindingResult br = invocation.getArgument(1);
            br.rejectValue("email", "duplicate", "Email already exists");
            return null;
        }).when(accountValidator).validateCreate(any(), any());

        mockMvc.perform(post("/admin/accounts/create")
                        .param("fullName", "Test User")
                        .param("email", "test@test.com")
                        .param("password", "Password1")
                        .param("role", "RECEPTIONIST"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/create"))
                .andExpect(model().attributeExists("view"));
    }

    @Test
    void updateAccount_shouldRedirect_onSuccess() throws Exception {
        var managerView = new AccountView(1L, "Test User", "test@test.com", "789", Role.MANAGER);
        when(accountService.getAccountView(1L)).thenReturn(managerView);

        mockMvc.perform(post("/admin/accounts/1/edit")
                        .param("fullName", "Test User")
                        .param("email", "test@test.com")
                        .param("phone", "1234567890")
                        .param("role", "RECEPTIONIST"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attributeExists(SUCCESS));

        verify(accountService).updateAccount(any(), any());
    }

    @Test
    void updateAccount_shouldReRender_onValidationError() throws Exception {
        var managerView = new AccountView(1L, "Test User", "test@test.com", "789", Role.MANAGER);
        when(accountService.getAccountView(1L)).thenReturn(managerView);

        mockMvc.perform(post("/admin/accounts/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/edit"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("accountId"));
    }

    @Test
    void updateAccount_shouldReRender_whenValidatorRejects() throws Exception {
        var managerView = new AccountView(1L, "Test User", "test@test.com", "789", Role.MANAGER);
        when(accountService.getAccountView(1L)).thenReturn(managerView);

        doAnswer(invocation -> {
            BindingResult br = invocation.getArgument(2);
            br.rejectValue("email", "duplicate", "Email already exists");
            return null;
        }).when(accountValidator).validateUpdate(any(), any(), any());

        mockMvc.perform(post("/admin/accounts/1/edit")
                        .param("fullName", "Test User")
                        .param("email", "test@test.com")
                        .param("phone", "1234567890")
                        .param("role", "RECEPTIONIST"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/edit"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("accountId"));
    }

    @Test
    void updateAccount_shouldRedirectWithError_whenDowngradingAdmin() throws Exception {
        var adminView = new AccountView(1L, "Alice Admin", "admin@test.com", "123", Role.ADMIN);
        when(accountService.getAccountView(1L)).thenReturn(adminView);

        mockMvc.perform(post("/admin/accounts/1/edit")
                        .param("fullName", "Alice Admin")
                        .param("email", "admin@test.com")
                        .param("role", "MANAGER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attribute(ERROR, "Cannot downgrade an admin account"));
    }

    @Test
    void updateAccount_shouldRedirectWithError_whenPromotingToAdmin() throws Exception {
        var managerView = new AccountView(2L, "Mike Manager", "mike@test.com", "456", Role.MANAGER);
        when(accountService.getAccountView(2L)).thenReturn(managerView);

        mockMvc.perform(post("/admin/accounts/2/edit")
                        .param("fullName", "Mike Manager")
                        .param("email", "mike@test.com")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attribute(ERROR, "Cannot promote a user to admin"));
    }

    @Test
    void deleteAccount_shouldRedirectWithSuccess() throws Exception {
        mockMvc.perform(post("/admin/accounts/delete/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attribute(SUCCESS, "Account deleted successfully"));

        verify(accountService).softDelete(2L);
    }

    @Test
    void deleteAccount_shouldRedirectWithError_whenIllegalState() throws Exception {
        doThrow(new IllegalStateException("Cannot delete an admin account"))
                .when(accountService).softDelete(1L);

        mockMvc.perform(post("/admin/accounts/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts/1"))
                .andExpect(flash().attribute(ERROR, "Cannot delete an admin account"));
    }

    @Test
    void deleteAccount_shouldRedirectWithError_whenDeletingSelf() throws Exception {
        Account account = new Account();
        account.setId(1L);
        account.setRole(Role.ADMIN);
        AccountPrincipal principal = new AccountPrincipal(account);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities()));
        try {
            mockMvc.perform(post("/admin/accounts/delete/1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/accounts"))
                    .andExpect(flash().attribute(ERROR, "You cannot delete your own account."));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void detailAccount_shouldReturn404_whenNotFound() throws Exception {
        when(accountService.getAccountView(999L)).thenThrow(new ResourceNotFoundException("Account"));

        mockMvc.perform(get("/admin/accounts/999"))
                .andExpect(status().is(404))
                .andExpect(view().name("error/404"));
    }

    @Test
    void editAccount_shouldReturn404_whenNotFound() throws Exception {
        when(accountService.getEditForm(999L)).thenThrow(new ResourceNotFoundException("Account"));

        mockMvc.perform(get("/admin/accounts/999/edit"))
                .andExpect(status().is(404))
                .andExpect(view().name("error/404"));
    }
}
