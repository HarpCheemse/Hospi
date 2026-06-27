package com.hospi.manage.features.account.validator;

import com.hospi.manage.features.account.dto.AccountCreateForm;
import com.hospi.manage.features.account.dto.AccountEditForm;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.repository.AccountRepository;
import com.hospi.manage.features.auth.dto.ResetPasswordForm;
import com.hospi.manage.features.credential.dto.ChangePasswordForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

/** Validator for account create, edit, and password-change forms. */
@Component
public class AccountValidator {
    private final AccountRepository accountRepository;

    public AccountValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Validate a change-password request.
     *
     * @param account the authenticated account
     * @param form    the change-password form data
     * @param br      binding result to populate with validation failures
     * @param encoder password encoder for verifying current password
     */
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

    /**
     * Validate a reset-password form.
     *
     * @param form          the reset-password form data
     * @param bindingResult binding result to populate with validation failures
     */
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

    /**
     * Validate a new-account creation form.
     *
     * @param form          the account creation form data
     * @param bindingResult binding result to populate with validation failures
     */
    public void validateCreate(AccountCreateForm form, BindingResult bindingResult) {
        validateEmail(form.email(),
                bindingResult);

        validatePassword(form.password(),
                "password",
                bindingResult);

        validateRole(form.role(),
                bindingResult);
    }

    /**
     * Validate an account-edit form.
     *
     * @param id            the account id being updated
     * @param form          the edit form data
     * @param bindingResult binding result to populate with validation failures
     */
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
