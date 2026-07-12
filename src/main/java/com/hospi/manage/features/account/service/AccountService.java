package com.hospi.manage.features.account.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.common.interfaces.EmailTemplates;
import com.hospi.manage.features.account.dto.AccountCreateForm;
import com.hospi.manage.features.account.dto.AccountEditForm;
import com.hospi.manage.features.account.dto.AccountView;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/** Business logic for staff account management (create, update, list). */
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /** Find an active account by ID. */
    public Account findById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account"));
        if (!account.isActive()) {
            throw new ResourceNotFoundException("Account");
        }
        return account;
    }

    /** Find an account by email address (any active status). */
    public Account findByEmail(String email) {
        return accountRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email"));
    }

    /** Return all active accounts as view models. */
    public List<AccountView> findAllViews() {
        return accountRepository.findAll().stream()
                .filter(Account::isActive)
                .map(account -> new AccountView(account.getId(),
                        account.getFullName(),
                        account.getEmail(),
                        account.getPhone(),
                        account.getRole())).toList();
    }

    /** Create a new staff account with an auto-generated password. Returns the raw password. */
    @Transactional
    public String createAccount(AccountCreateForm form) {
        Account account = new Account();
        account.setFullName(form.fullName());
        account.setEmail(form.email());
        account.setPhone(form.phone());

        byte[] randomBytes = new byte[12];
        new SecureRandom().nextBytes(randomBytes);
        String rawPassword = Base64.getEncoder().encodeToString(randomBytes);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));

        account.setRole(form.role());
        accountRepository.save(account);

        var template = EmailTemplates.welcome(form.fullName(), form.email(), rawPassword, form.role().name());
        emailService.send(form.email(), template.subject(), template.content());

        return rawPassword;
    }

    /** Return the account view model for the given ID. */
    public AccountView getAccountView(Long id) {
        Account account = findById(id);
        return new AccountView(account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getRole());
    }

    /** Return the edit form populated with existing account data. */
    public AccountEditForm getEditForm(Long id) {
        Account account = findById(id);

        return new AccountEditForm(account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getRole());
    }

    /** Update an existing account's personal details, role, and status. */
    @Transactional
    public void updateAccount(Long id, AccountEditForm form) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account " + id));
        if (!account.isActive()) {
            throw new ResourceNotFoundException("Account " + id);
        }

        account.setFullName(form.fullName());
        account.setEmail(form.email());
        account.setPhone(form.phone());
        account.setRole(form.role());

        accountRepository.save(account);
    }

    /** Soft-delete an account by marking it inactive. */
    @Transactional
    public void softDelete(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account " + id));
        if (!account.isActive()) {
            throw new ResourceNotFoundException("Account " + id);
        }
        if (account.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Cannot delete an admin account");
        }
        account.setActive(false);
        accountRepository.save(account);
    }

    /** Re-hash all account passwords with the given raw password value. */
    @Transactional
    public void rehashAllPasswords(String defaultRawPassword) {
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            String newHash = passwordEncoder.encode(defaultRawPassword);
            account.setPasswordHash(newHash);
        }
        accountRepository.saveAll(accounts);
    }
}
