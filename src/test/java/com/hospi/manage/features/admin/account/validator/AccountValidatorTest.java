package com.hospi.manage.features.admin.account.validator;

import com.hospi.manage.features.admin.account.dto.AccountCreateForm;
import com.hospi.manage.features.admin.account.dto.AccountEditForm;
import com.hospi.manage.features.admin.account.enums.AccountStatus;
import com.hospi.manage.features.admin.account.enums.Role;
import com.hospi.manage.features.admin.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountValidatorTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountValidator accountValidator;

    @Test
    void shouldRejectWhenEmailAlreadyExists() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "Password123",
                "0123456789",
                Role.MANAGER
        );

        when(accountRepository.existsByEmail("john@hospi.com"))
                .thenReturn(true);

        BindingResult bindingResult =
                new BeanPropertyBindingResult(form, "form");

        accountValidator.validateCreate(form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("email"));
    }

    @Test
    void shouldAcceptWhenEmailDoesNotExist() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "Password123",
                "0123456789",
                Role.MANAGER
        );

        when(accountRepository.existsByEmail("john@hospi.com"))
                .thenReturn(false);

        BindingResult bindingResult =
                new BeanPropertyBindingResult(form, "form");

        accountValidator.validateCreate(form, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    void shouldRejectWhenPasswordIsTooShort() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "abc12",
                "0123456789",
                Role.MANAGER
        );

        BindingResult bindingResult =
                new BeanPropertyBindingResult(form, "form");

        accountValidator.validateCreate(form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("password"));
    }

    @Test
    void shouldRejectWhenPasswordContainsNoDigit() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "PasswordOnly",
                "0123456789",
                Role.MANAGER
        );

        BindingResult bindingResult =
                new BeanPropertyBindingResult(form, "form");

        accountValidator.validateCreate(form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("password"));
    }

    @Test
    void shouldRejectWhenPasswordContainsNoLetter() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "12345678",
                "0123456789",
                Role.MANAGER
        );

        BindingResult bindingResult =
                new BeanPropertyBindingResult(form, "form");

        accountValidator.validateCreate(form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("password"));
    }

    @Test
    void shouldRejectWhenRoleIsAdmin() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "Password123",
                "0123456789",
                Role.ADMIN
        );

        BindingResult bindingResult =
                new BeanPropertyBindingResult(form, "form");

        accountValidator.validateCreate(form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("role"));
    }

    @Test
    void shouldAcceptValidAccount() {

        AccountCreateForm form = new AccountCreateForm(
                "John Doe",
                "john@hospi.com",
                "Password123",
                "0123456789",
                Role.MANAGER
        );

        when(accountRepository.existsByEmail("john@hospi.com"))
                .thenReturn(false);

        BindingResult bindingResult =
                new BeanPropertyBindingResult(form, "form");

        accountValidator.validateCreate(form, bindingResult);

        assertFalse(bindingResult.hasErrors());
    }

    @Test
    void shouldRejectWhenEmailBelongsToAnotherAccount() {

        Long currentAccountId = 1L;

        AccountEditForm form = new AccountEditForm(
                "John Doe",
                "john@hospi.com",
                "0123456789",
                Role.MANAGER,
                AccountStatus.ACTIVE
        );

        when(accountRepository.existsByEmailAndIdNot("john@hospi.com", currentAccountId))
                .thenReturn(true);

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        accountValidator.validateUpdate(currentAccountId, form, bindingResult);

        assertTrue(bindingResult.hasFieldErrors("email"));
    }

}