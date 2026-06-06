package com.hospi.manage.features.credential.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.core.security.session.AccountPrincipal;
import com.hospi.manage.features.admin.account.enums.Role;
import com.hospi.manage.features.admin.account.validator.AccountValidator;
import com.hospi.manage.features.credential.dto.ChangePasswordForm;
import com.hospi.manage.features.credential.dto.CredentialView;
import com.hospi.manage.features.credential.service.CredentialService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/credentials")
public class CredentialController {

    private final CredentialService credentialService;
    private final AccountValidator accountValidator;
    private final PasswordEncoder passwordEncoder;

    public CredentialController(CredentialService credentialService, AccountValidator accountValidator,
                                PasswordEncoder passwordEncoder) {
        this.credentialService = credentialService;
        this.accountValidator = accountValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute("activeSidebar",
                "CREDENTIALS");
    }

    private String credentialViewPath(Role role) {
        return switch (role) {
            case ADMIN -> "admin/credential";
            case MANAGER -> "manager/credential";
            case RECEPTIONIST -> "receptionist/credential";
            case LEADER -> "leader/credential";
        };
    }

    @GetMapping
    public String credentials(@AuthenticationPrincipal AccountPrincipal principal, Model model) {
        CredentialView view = credentialService.getCredentialView(principal.getAccount().getId());

        model.addAttribute("view",
                view);

        return credentialViewPath(principal.getAccount().getRole());
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute(Attributes.FORM,
                new ChangePasswordForm(null,
                        null,
                        null));
        return "auth/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal AccountPrincipal principal,
                                 @Valid @ModelAttribute(Attributes.FORM) ChangePasswordForm form,
                                 BindingResult bindingResult) {
        accountValidator.validateChangePassword(
                principal.getAccount(),
                form,
                bindingResult,
                passwordEncoder);

        if (bindingResult.hasErrors()) {
            return "auth/change-password";
        }

        credentialService.changePassword(principal.getAccount(),
                form.newPassword());

        return "redirect:/credentials";
    }
}