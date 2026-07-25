package com.hospi.manage.features.account.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.common.utils.SecurityUtils;
import com.hospi.manage.features.account.dto.AccountCreateForm;
import com.hospi.manage.features.account.dto.AccountEditForm;
import com.hospi.manage.features.account.dto.AccountView;
import com.hospi.manage.features.account.dto.response.AccountCreateView;
import com.hospi.manage.features.account.dto.response.AccountListView;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.service.AccountService;
import com.hospi.manage.features.account.validator.AccountValidator;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.audit.enums.AuditAction;
import com.hospi.manage.core.security.session.AccountPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Controller for admin account management (CRUD for staff accounts). */
@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final AccountValidator accountValidator;
    private final AuditService auditService;

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "STAFF_ACCOUNTS");
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) Role role,
                       Model model) {
        List<AccountView> all = accountService.findAllViews();
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            all = all.stream()
                    .filter(a -> a.fullName().toLowerCase().contains(q) || a.email().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }
        if (role != null) {
            all = all.stream()
                    .filter(a -> a.role() == role)
                    .collect(Collectors.toList());
        }
        model.addAttribute(Attributes.VIEW, new AccountListView(
                all, Arrays.asList(Role.values()),
                role != null ? role.name() : null, search));
        return "account/list";
    }

    @GetMapping("/create")
    String create(Model model) {
        model.addAttribute(Attributes.FORM, new AccountCreateForm(null, null, null, null));
        model.addAttribute(Attributes.VIEW, new AccountCreateView(availableRoles()));
        return "account/create";
    }

    @PostMapping("/create")
    public String createAccount(@Valid @ModelAttribute(Attributes.FORM) AccountCreateForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        accountValidator.validateCreate(form, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute(Attributes.VIEW, new AccountCreateView(availableRoles()));
            return "account/create";
        }

        accountService.createAccount(form);
        auditService.log(null, SecurityUtils.currentStaffName(), AuditAction.CREATE, "ACCOUNT", null,
                "Created account: " + form.email());
        redirectAttributes.addFlashAttribute(Attributes.SUCCESS, "Account created. Login credentials sent via email.");
        return "redirect:/admin/accounts";
    }

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        model.addAttribute("account", accountService.getAccountView(id));
        return "account/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute(Attributes.FORM, accountService.getEditForm(id));
        model.addAttribute("roles", availableRoles());
        model.addAttribute("accountId", id);
        return "account/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateAccount(@PathVariable Long id,
                                @Valid @ModelAttribute(Attributes.FORM) AccountEditForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        AccountView current = accountService.getAccountView(id);
        if (current.role() == Role.ADMIN && form.role() != Role.ADMIN) {
            redirectAttributes.addFlashAttribute(Attributes.ERROR, "Cannot downgrade an admin account");
            return "redirect:/admin/accounts";
        }
        if (current.role() != Role.ADMIN && form.role() == Role.ADMIN) {
            redirectAttributes.addFlashAttribute(Attributes.ERROR, "Cannot promote a user to admin");
            return "redirect:/admin/accounts";
        }

        accountValidator.validateUpdate(id, form, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", availableRoles());
            model.addAttribute("accountId", id);
            return "account/edit";
        }

        accountService.updateAccount(id, form);
        auditService.log(null, SecurityUtils.currentStaffName(), AuditAction.UPDATE, "ACCOUNT", id,
                "Updated account: " + form.email());
        redirectAttributes.addFlashAttribute(Attributes.SUCCESS, "Account updated successfully");
        return "redirect:/admin/accounts";
    }

    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id,
                                @AuthenticationPrincipal AccountPrincipal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal != null && id.equals(principal.getAccount().getId())) {
            redirectAttributes.addFlashAttribute(Attributes.ERROR, "You cannot delete your own account.");
            return "redirect:/admin/accounts";
        }
        try {
            accountService.softDelete(id);
            auditService.log(null, SecurityUtils.currentStaffName(), AuditAction.DELETE, "ACCOUNT", id, "Deleted account");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(Attributes.ERROR, e.getMessage());
            return "redirect:/admin/accounts/" + id;
        }
        redirectAttributes.addFlashAttribute(Attributes.SUCCESS, "Account deleted successfully");
        return "redirect:/admin/accounts";
    }

    private List<Role> availableRoles() {
        return Arrays.stream(Role.values()).filter(r -> r != Role.ADMIN).toList();
    }
}
