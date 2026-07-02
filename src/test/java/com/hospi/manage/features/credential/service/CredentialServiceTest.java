package com.hospi.manage.features.credential.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.repository.AccountRepository;
import com.hospi.manage.features.credential.dto.CredentialView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private CredentialService credentialService;

    @BeforeEach
    void setUp() {
        credentialService = new CredentialService(accountRepository, passwordEncoder);
    }

    @Test
    void getCredentialView_shouldReturnView_whenFound() {
        var now = LocalDateTime.now();
        var account = new Account();
        account.setId(1L);
        account.setFullName("John Doe");
        account.setEmail("john@test.com");
        account.setPhone("1234567890");
        account.setRole(Role.RECEPTIONIST);
        account.setActive(true);
        account.setCreatedAt(now);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        CredentialView view = credentialService.getCredentialView(1L);

        assertEquals("John Doe", view.name());
        assertEquals("john@test.com", view.email());
        assertEquals("RECEPTIONIST", view.role());
        assertEquals("1234567890", view.phone());
        assertEquals("Active", view.status());
        assertEquals(now, view.createdAt());
    }

    @Test
    void getCredentialView_shouldThrow_whenNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> credentialService.getCredentialView(1L));
    }

    @Test
    void changePassword_shouldEncodeAndSave() {
        var account = new Account();
        account.setId(1L);
        account.setPasswordHash("oldHash");

        when(passwordEncoder.encode("newPass")).thenReturn("hashedNewPass");

        credentialService.changePassword(account, "newPass");

        verify(passwordEncoder).encode("newPass");
        verify(accountRepository).save(accountCaptor.capture());
        Account saved = accountCaptor.getValue();
        assertEquals("hashedNewPass", saved.getPasswordHash());
        assertTrue(saved == account);
    }
}

