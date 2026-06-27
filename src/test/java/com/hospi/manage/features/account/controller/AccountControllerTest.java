package com.hospi.manage.features.account.controller;

import com.hospi.manage.features.account.dto.AccountCreateForm;
import com.hospi.manage.features.account.dto.AccountEditForm;
import com.hospi.manage.features.account.dto.AccountView;
import com.hospi.manage.features.account.enums.AccountStatus;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.service.AccountService;
import com.hospi.manage.features.account.validator.AccountValidator;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.util.List;

import static com.hospi.manage.common.constant.Attributes.FORM;
import static com.hospi.manage.common.constant.Attributes.SUCCESS;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

    @Test
    void list_shouldRender() throws Exception {
        var accounts = List.of(
                new AccountView(1L, "Alice Admin", "alice@test.com", "123", Role.ADMIN, AccountStatus.ACTIVE),
                new AccountView(2L, "Bob Receptionist", "bob@test.com", "456", Role.RECEPTIONIST, AccountStatus.ACTIVE)
        );
        when(accountService.findAllViews()).thenReturn(accounts);

        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/list"))
                .andExpect(model().attributeExists("accounts"))
                .andExpect(content().string(containsString("Account Management")));
    }

    @Test
    void create_shouldRender() throws Exception {
        mockMvc.perform(get("/admin/accounts/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/create"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists(FORM))
                .andExpect(content().string(containsString("Staff Accounts")));
    }

    @Test
    void detail_shouldRender() throws Exception {
        var account = new AccountView(1L, "Test User", "test@test.com", "789", Role.MANAGER, AccountStatus.ACTIVE);
        when(accountService.getAccountView(1L)).thenReturn(account);

        mockMvc.perform(get("/admin/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/detail"))
                .andExpect(model().attributeExists("account"))
                .andExpect(content().string(containsString("Test User")));
    }

    @Test
    void edit_shouldRender() throws Exception {
        var editForm = new AccountEditForm("Test User", "test@test.com", "789", Role.MANAGER, AccountStatus.ACTIVE);
        when(accountService.getEditForm(1L)).thenReturn(editForm);

        mockMvc.perform(get("/admin/accounts/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/edit"))
                .andExpect(model().attributeExists(FORM))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("accountId"))
                .andExpect(content().string(containsString("Edit Account")));
    }

    @Test
    void createAccount_shouldRedirect_onSuccess() throws Exception {
        mockMvc.perform(post("/admin/accounts/create")
                        .param("fullName", "Test User")
                        .param("email", "test@test.com")
                        .param("password", "Password1")
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
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("statuses"));
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
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("statuses"));
    }

    @Test
    void updateAccount_shouldRedirect_onSuccess() throws Exception {
        mockMvc.perform(post("/admin/accounts/1/edit")
                        .param("fullName", "Test User")
                        .param("email", "test@test.com")
                        .param("role", "RECEPTIONIST")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attributeExists(SUCCESS));

        verify(accountService).updateAccount(any(), any());
    }

    @Test
    void updateAccount_shouldReRender_onValidationError() throws Exception {
        mockMvc.perform(post("/admin/accounts/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/edit"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("accountId"));
    }

    @Test
    void updateAccount_shouldReRender_whenValidatorRejects() throws Exception {
        doAnswer(invocation -> {
            BindingResult br = invocation.getArgument(2);
            br.rejectValue("email", "duplicate", "Email already exists");
            return null;
        }).when(accountValidator).validateUpdate(any(), any(), any());

        mockMvc.perform(post("/admin/accounts/1/edit")
                        .param("fullName", "Test User")
                        .param("email", "test@test.com")
                        .param("role", "RECEPTIONIST")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/edit"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("accountId"));
    }
}
