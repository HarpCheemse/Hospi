package com.hospi.manage.features.account.validator;

import com.hospi.manage.features.account.dto.AccountCreateForm;
import com.hospi.manage.features.account.dto.AccountEditForm;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.repository.AccountRepository;
import com.hospi.manage.features.credential.dto.ChangePasswordForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountValidatorTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountValidator accountValidator;

    private BindingResult binding(Object target) {
        return new BeanPropertyBindingResult(target,
                "form");
    }

    private Account mockAccount(String encodedPassword) {
        Account acc = mock(Account.class);
        when(acc.getPasswordHash()).thenReturn(encodedPassword);
        return acc;
    }

    // ---------------- CREATE VALIDATION ----------------

    @Test
    void shouldRejectWhenEmailAlreadyExists() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "0123456789",
                Role.MANAGER
        );

        when(accountRepository.existsByEmail("john@hospi.com")).thenReturn(true);

        BindingResult br = binding(form);

        accountValidator.validateCreate(form,
                br);

        assertTrue(br.hasFieldErrors("email"));
    }

    @Test
    void shouldAcceptWhenEmailDoesNotExist() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "0123456789",
                Role.MANAGER
        );

        when(accountRepository.existsByEmail("john@hospi.com")).thenReturn(false);

        BindingResult br = binding(form);

        accountValidator.validateCreate(form,
                br);

        assertFalse(br.hasErrors());
    }

    @Test
    void shouldRejectWhenRoleIsAdmin() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "0123456789",
                Role.ADMIN
        );

        BindingResult br = binding(form);

        accountValidator.validateCreate(form,
                br);

        assertTrue(br.hasFieldErrors("role"));
    }

    @Test
    void shouldAcceptValidAccount() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "0123456789",
                Role.MANAGER
        );

        when(accountRepository.existsByEmail("john@hospi.com")).thenReturn(false);

        BindingResult br = binding(form);

        accountValidator.validateCreate(form,
                br);

        assertFalse(br.hasErrors());
    }

    // ---------------- UPDATE VALIDATION ----------------

    @Test
    void shouldRejectWhenEmailBelongsToAnotherAccount() {

        Long id = 1L;

        AccountEditForm form = new AccountEditForm(
                "John Doe",
                "john@hospi.com",
                "0123456789",
                Role.MANAGER
        );

        when(accountRepository.existsByEmailAndIdNot("john@hospi.com",
                id))
                .thenReturn(true);

        BindingResult br = binding(form);

        accountValidator.validateUpdate(id,
                form,
                br);

        assertTrue(br.hasFieldErrors("email"));
    }

    // ---------------- CHANGE PASSWORD ----------------

    @Test
    void shouldAcceptValidChangePassword() {

        Account account = mockAccount("encodedOldPass");

        ChangePasswordForm form = new ChangePasswordForm(
                "oldPass",
                "Newpass123",
                "Newpass123"
        );

        BindingResult br = binding(form);

        when(passwordEncoder.matches("oldPass",
                "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.matches("Newpass123",
                "encodedOldPass")).thenReturn(false);

        accountValidator.validateChangePassword(account,
                form,
                br,
                passwordEncoder);

        assertFalse(br.hasErrors());
    }
}
