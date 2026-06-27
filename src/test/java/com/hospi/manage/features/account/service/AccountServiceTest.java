package com.hospi.manage.features.account.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.account.dto.AccountCreateForm;
import com.hospi.manage.features.account.dto.AccountEditForm;
import com.hospi.manage.features.account.dto.AccountView;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.AccountStatus;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.repository.AccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, passwordEncoder);
    }

    @Test
    void shouldReturnAccount_whenFound() {
        Account account = new Account();
        account.setId(1L);
        account.setFullName("John Doe");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Account result = accountService.findById(1L);

        assertSame(account, result);
        assertEquals("John Doe", result.getFullName());
    }

    @Test
    void shouldThrow_whenNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.findById(1L));
    }

    @Test
    void shouldReturnAccount_whenEmailFound() {
        Account account = new Account();
        account.setEmail("john@test.com");
        when(accountRepository.findByEmail("john@test.com")).thenReturn(Optional.of(account));

        Account result = accountService.findByEmail("john@test.com");

        assertSame(account, result);
        assertEquals("john@test.com", result.getEmail());
    }

    @Test
    void shouldThrow_whenEmailNotFound() {
        when(accountRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.findByEmail("missing@test.com"));
    }

    @Test
    void shouldReturnListOfAccountViews() {
        Account account1 = new Account();
        account1.setId(1L);
        account1.setFullName("Alice");
        account1.setEmail("alice@test.com");
        account1.setPhone("111");
        account1.setRole(Role.ADMIN);
        account1.setStatus(AccountStatus.ACTIVE);

        Account account2 = new Account();
        account2.setId(2L);
        account2.setFullName("Bob");
        account2.setEmail("bob@test.com");
        account2.setPhone("222");
        account2.setRole(Role.RECEPTIONIST);
        account2.setStatus(AccountStatus.DISABLED);

        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

        List<AccountView> views = accountService.findAllViews();

        assertEquals(2, views.size());
        assertEquals(1L, views.get(0).id());
        assertEquals("Alice", views.get(0).fullName());
        assertEquals("alice@test.com", views.get(0).email());
        assertEquals("111", views.get(0).phone());
        assertEquals(Role.ADMIN, views.get(0).role());
        assertEquals(AccountStatus.ACTIVE, views.get(0).status());
        assertEquals(2L, views.get(1).id());
        assertEquals("Bob", views.get(1).fullName());
        assertEquals("bob@test.com", views.get(1).email());
        assertEquals("222", views.get(1).phone());
        assertEquals(Role.RECEPTIONIST, views.get(1).role());
        assertEquals(AccountStatus.DISABLED, views.get(1).status());
    }

    @Test
    void shouldCreateAccount_withDisabledStatus() {
        AccountCreateForm form = new AccountCreateForm("John", "john@test.com", "rawPass", "555", Role.RECEPTIONIST);
        when(passwordEncoder.encode("rawPass")).thenReturn("hashedValue");

        accountService.createAccount(form);

        verify(passwordEncoder).encode("rawPass");
        verify(accountRepository).save(accountCaptor.capture());
        Account saved = accountCaptor.getValue();
        assertEquals("John", saved.getFullName());
        assertEquals("john@test.com", saved.getEmail());
        assertEquals("555", saved.getPhone());
        assertEquals("hashedValue", saved.getPasswordHash());
        assertEquals(Role.RECEPTIONIST, saved.getRole());
        assertEquals(AccountStatus.DISABLED, saved.getStatus());
    }

    @Test
    void shouldReturnAccountView() {
        Account account = new Account();
        account.setId(1L);
        account.setFullName("Alice");
        account.setEmail("alice@test.com");
        account.setPhone("111");
        account.setRole(Role.ADMIN);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountView view = accountService.getAccountView(1L);

        assertEquals(1L, view.id());
        assertEquals("Alice", view.fullName());
        assertEquals("alice@test.com", view.email());
        assertEquals("111", view.phone());
        assertEquals(Role.ADMIN, view.role());
        assertEquals(AccountStatus.ACTIVE, view.status());
    }

    @Test
    void shouldReturnEditForm() {
        Account account = new Account();
        account.setId(1L);
        account.setFullName("Bob");
        account.setEmail("bob@test.com");
        account.setPhone("222");
        account.setRole(Role.MANAGER);
        account.setStatus(AccountStatus.DISABLED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountEditForm form = accountService.getEditForm(1L);

        assertEquals("Bob", form.fullName());
        assertEquals("bob@test.com", form.email());
        assertEquals("222", form.phone());
        assertEquals(Role.MANAGER, form.role());
        assertEquals(AccountStatus.DISABLED, form.status());
    }

    @Test
    void shouldUpdateAccountFields() {
        Account account = new Account();
        account.setId(1L);
        account.setFullName("Old Name");
        account.setEmail("old@test.com");
        account.setPhone("000");
        account.setRole(Role.RECEPTIONIST);
        account.setStatus(AccountStatus.DISABLED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountEditForm form = new AccountEditForm("New Name", "new@test.com", "999", Role.ADMIN, AccountStatus.ACTIVE);
        accountService.updateAccount(1L, form);

        verify(accountRepository).save(account);
        assertEquals("New Name", account.getFullName());
        assertEquals("new@test.com", account.getEmail());
        assertEquals("999", account.getPhone());
        assertEquals(Role.ADMIN, account.getRole());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void shouldThrow_whenUpdatingNonExistent() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        AccountEditForm form = new AccountEditForm("N", "n@t.com", "0", Role.RECEPTIONIST, AccountStatus.ACTIVE);

        assertThrows(ResourceNotFoundException.class, () -> accountService.updateAccount(1L, form));
    }

    @Test
    void shouldRehashAllPasswords() {
        Account account1 = new Account();
        account1.setId(1L);
        account1.setPasswordHash("old1");
        Account account2 = new Account();
        account2.setId(2L);
        account2.setPasswordHash("old2");

        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));
        when(passwordEncoder.encode("defaultPass")).thenReturn("newHash");

        accountService.rehashAllPasswords("defaultPass");

        verify(passwordEncoder, times(2)).encode("defaultPass");
        assertEquals("newHash", account1.getPasswordHash());
        assertEquals("newHash", account2.getPasswordHash());
        verify(accountRepository).saveAll(List.of(account1, account2));
    }
}
