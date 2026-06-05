package com.hospi.manage.features.admin.account.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.core.security.Argon2HashingService;
import com.hospi.manage.features.admin.account.dto.AccountCreateForm;
import com.hospi.manage.features.admin.account.dto.AccountEditForm;
import com.hospi.manage.features.admin.account.dto.AccountView;
import com.hospi.manage.features.admin.account.entity.Account;
import com.hospi.manage.features.admin.account.enums.AccountStatus;
import com.hospi.manage.features.admin.account.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final Argon2HashingService argon2HashingService;

    AccountService(AccountRepository accountRepository,
                   Argon2HashingService argon2HashingService) {
        this.argon2HashingService = argon2HashingService;
        this.accountRepository = accountRepository;
    }

    public Account findById(Long id) {
        return accountRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Account")
        );
    }

    public List<AccountView> findAllViews() {
        return accountRepository.findAll()
                .stream()
                .map(account -> new AccountView(
                        account.getId(),
                        account.getFullName(),
                        account.getEmail(),
                        account.getPhone(),
                        account.getRole(),
                        account.getStatus()
                ))
                .toList();
    }

    @Transactional
    public void createAccount(AccountCreateForm form) {
        Account account = new Account();

        account.setHotelId(HotelConstants.HOTEL_ID);

        account.setFullName(form.fullName());
        account.setEmail(form.email());
        account.setPhone(form.phone());

        String passwordHash = argon2HashingService.hash(form.password());

        account.setPasswordHash(passwordHash);

        account.setRole(form.role());
        account.setStatus(AccountStatus.DISABLED);

        accountRepository.save(account);
    }

    public AccountView getAccountView(Long id) {
        Account account = findById(id);
        return new AccountView(
                account.getId(),
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getRole(),
                account.getStatus()
        );
    }

    public AccountEditForm getEditForm(Long id) {
        Account account = findById(id);

        return new AccountEditForm(
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getRole(),
                account.getStatus()
        );
    }

    @Transactional
    public void updateAccount(Long id, AccountEditForm form) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found with id: " + id)
                );

        account.setFullName(form.fullName());
        account.setEmail(form.email());
        account.setPhone(form.phone());
        account.setRole(form.role());
        account.setStatus(form.status());

        accountRepository.save(account);
    }
}
