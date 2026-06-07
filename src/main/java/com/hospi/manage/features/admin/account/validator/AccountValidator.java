package com.hospi.manage.features.admin.account.validator;

import com.hospi.manage.features.admin.account.dto.AccountCreateForm;
import com.hospi.manage.features.admin.account.dto.AccountEditForm;
import com.hospi.manage.features.admin.account.entity.Account;
import com.hospi.manage.features.admin.account.enums.Role;
import com.hospi.manage.features.admin.account.repository.AccountRepository;
import com.hospi.manage.features.auth.dto.ResetPasswordForm;
import com.hospi.manage.features.credential.dto.ChangePasswordForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class AccountValidator {
    private final AccountRepository accountRepository;

    AccountValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void validateChangePassword(Account account, ChangePasswordForm form, BindingResult br,
                                       PasswordEncoder encoder) {

        if (br.hasErrors()) return;

        validateCurrentPassword(account,
                form.currentPassword(),
                encoder,
                br);
        if (br.hasErrors()) return;

        validateNewPassword(form.newPassword(),
                br);
        if (br.hasErrors()) return;

        validatePasswordMatch(form.newPassword(),
                form.confirmPassword(),
                br);
        if (br.hasErrors()) return;

        validateNotSameAsOld(account,
                form.newPassword(),
                encoder,
                br);
    }

    public void validateResetPassword(ResetPasswordForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return;

        validateNewPassword(form.newPassword(),
                bindingResult);

        if (bindingResult.hasErrors()) return;

        validatePasswordMatch(form.newPassword(),
                form.confirmPassword(),
                bindingResult);
    }

    private void validatePasswordMatch(String newPassword, String confirmPassword, BindingResult br) {
        if (newPassword != null && !newPassword.equals(confirmPassword)) {
            br.rejectValue("confirmPassword",
                    "mismatch.password",
                    "Passwords do not match");
        }
    }

    private void validateNotSameAsOld(Account account, String newPassword, PasswordEncoder encoder, BindingResult br) {
        if (encoder.matches(newPassword,
                account.getPasswordHash())) {
            br.rejectValue("newPassword",
                    "same.password",
                    "New password must be different from old password");
        }
    }

    private void validateNewPassword(String newPassword, BindingResult br) {

        validatePassword(newPassword,
                "newPassword",
                br);
    }

    private void validateCurrentPassword(Account account, String currentPassword, PasswordEncoder encoder,
                                         BindingResult br) {
        if (!encoder.matches(currentPassword,
                account.getPasswordHash())) {
            br.rejectValue("currentPassword",
                    "invalid.currentPassword",
                    "Current password is incorrect");
        }
    }

    public void validateCreate(AccountCreateForm form, BindingResult bindingResult) {
        validateEmail(form.email(),
                bindingResult);

        validatePassword(form.password(),
                "password",
                bindingResult);

        validateRole(form.role(),
                bindingResult);
    }

    public void validateUpdate(Long id, AccountEditForm form, BindingResult bindingResult) {
        validateRole(form.role(),
                bindingResult);

        validateEmailUpdate(id,
                form.email(),
                bindingResult);
    }

    private void validateEmailUpdate(Long id, String email, BindingResult bindingResult) {

        if (accountRepository.existsByEmailAndIdNot(email,
                id)) {
            bindingResult.rejectValue("email",
                    "email.duplicate",
                    "Email is already in use");
        }
    }

    private void validateRole(Role role, BindingResult bindingResult) {

        if (role == null) {
            return;
        }

        if (role == Role.ADMIN) {
            bindingResult.rejectValue("role",
                    "role.not.allowed",
                    "Admin accounts cannot be created");
        }
    }

    private void validateEmail(String email, BindingResult bindingResult) {
        if (accountRepository.existsByEmail(email)) {
            bindingResult.rejectValue("email",
                    "duplicate",
                    "Email already exists");
        }
    }


    private void validatePassword(String password, String fieldName, BindingResult bindingResult) {

        if (!isValidPassword(password)) {
            bindingResult.rejectValue(fieldName,
                    "invalid.password",
                    "Password must be at least 8 characters and contain both letters and numbers");
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
