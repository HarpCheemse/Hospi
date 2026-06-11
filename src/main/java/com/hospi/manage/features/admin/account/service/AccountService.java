package com.hospi.manage.features.admin.account.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.admin.account.dto.AccountCreateForm;
import com.hospi.manage.features.admin.account.dto.AccountEditForm;
import com.hospi.manage.features.admin.account.dto.AccountView;
import com.hospi.manage.features.admin.account.entity.Account;
import com.hospi.manage.features.admin.account.enums.AccountStatus;
import com.hospi.manage.features.admin.account.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }

    public Account findById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account"));
    }

    public Account findByEmail(String email) {
        return accountRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email"));
    }

    public List<AccountView> findAllViews() {
        return accountRepository.findAll().stream().map(account -> new AccountView(account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getRole(),
                account.getStatus())).toList();
    }

    @Transactional
    public void createAccount(AccountCreateForm form) {
        Account account = new Account();

        account.setFullName(form.fullName());
        account.setEmail(form.email());
        account.setPhone(form.phone());

        String passwordHash = passwordEncoder.encode(form.password());

        account.setPasswordHash(passwordHash);

        account.setRole(form.role());
        account.setStatus(AccountStatus.DISABLED);

        accountRepository.save(account);
    }

    public AccountView getAccountView(Long id) {
        Account account = findById(id);
        return new AccountView(account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getRole(),
                account.getStatus());
    }

    public AccountEditForm getEditForm(Long id) {
        Account account = findById(id);

        return new AccountEditForm(account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getRole(),
                account.getStatus());
    }

    @Transactional
    public void updateAccount(Long id, AccountEditForm form) {

        Account account = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        account.setFullName(form.fullName());
        account.setEmail(form.email());
        account.setPhone(form.phone());
        account.setRole(form.role());
        account.setStatus(form.status());

        accountRepository.save(account);
    }

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
