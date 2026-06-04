package com.hospi.manage.features.admin.account.validator;

import com.hospi.manage.features.admin.account.dto.AccountCreateForm;
import com.hospi.manage.features.admin.account.enums.Role;
import com.hospi.manage.features.admin.account.repository.AccountRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

@Component
public class AccountValidator {
    private final AccountRepository accountRepository;

    AccountValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void validateCreate(AccountCreateForm form, BindingResult bindingResult) {
        validateEmail(form.email(), bindingResult);

        validatePassword(form.password(), bindingResult);

        validateRole(form.role(), bindingResult);
    }

    private void validateRole(Role role,
                              BindingResult bindingResult) {

        if (role == null) {
            return;
        }

        if (role == Role.ADMIN) {
            bindingResult.rejectValue(
                    "role",
                    "role.not.allowed",
                    "Admin accounts cannot be created"
            );
        }
    }

    private void validateEmail(String email, BindingResult bindingResult) {
        if (accountRepository.existsByEmail(email)) {
            bindingResult.rejectValue(
                    "email",
                    "duplicate",
                    "Email already exists"
            );
        }
    }


    private void validatePassword(String password,
                                  BindingResult bindingResult) {

        if (!isValidPassword(password)) {
            bindingResult.rejectValue(
                    "password",
                    "invalid.password",
                    "Password must be at least 8 characters and contain both letters and numbers"
            );
        }
    }

    private boolean isValidPassword(String password) {

        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {

            if (Character.isLetter(c)) {
                hasLetter = true;
            }

            if (Character.isDigit(c)) {
                hasDigit = true;
            }

            if (hasLetter && hasDigit) {
                return true;
            }
        }

        return false;
    }

}
