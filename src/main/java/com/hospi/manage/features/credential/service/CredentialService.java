package com.hospi.manage.features.credential.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.repository.AccountRepository;
import com.hospi.manage.features.credential.dto.CredentialView;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Business logic for credential viewing and password changes. */
@Service
public class CredentialService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder encoder;


    public CredentialService(AccountRepository accountRepository,
                             PasswordEncoder encoder) {
        this.accountRepository = accountRepository;
        this.encoder = encoder;
    }

    /** Return the credential view model for the given account ID. */
    public CredentialView getCredentialView(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Credentials")
        );
        CredentialView view = new CredentialView(
                account.getFullName(),
                account.getEmail(),
                account.getRole().name(),
                account.getPhone(),
                account.isActive() ? "Active" : "Inactive",
                account.getCreatedAt());

        return view;
    }

    /** Hash and persist a new password for the given account. */
    public void changePassword(Account account, String newPassword) {
        account.setPasswordHash(encoder.encode(newPassword));

        accountRepository.save(account);
    }
}
