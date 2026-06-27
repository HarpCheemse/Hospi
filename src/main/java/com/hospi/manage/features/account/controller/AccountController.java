package com.hospi.manage.features.account.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.account.dto.AccountCreateForm;
import com.hospi.manage.features.account.dto.AccountEditForm;
import com.hospi.manage.features.account.dto.AccountView;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.account.service.AccountService;
import com.hospi.manage.features.account.validator.AccountValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for admin account management (CRUD for staff accounts).
 */
@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final AccountValidator accountValidator;

    /**
     * Set the active sidebar highlight for this feature.
     */
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "STAFF_ACCOUNTS");
    }

    /**
     * Show the account list page with optional filtering by search text and role.
     */
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
        model.addAttribute("accounts", all);
        model.addAttribute("roles", Role.values());
        return "account/list";
    }

    /**
     * Show the account creation form.
     */
    @GetMapping("/create")
    String create(Model model) {
        model.addAttribute(Attributes.FORM, new AccountCreateForm(null, null, null, null, null));
        model.addAttribute("roles", Role.values());
        return "account/create";
    }

    /**
     * Show details for a single account.
     */
    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        model.addAttribute("account", accountService.getAccountView(id));
        return "account/detail";
    }

    /**
     * Show the account edit form pre-populated with current values.
     */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute(Attributes.FORM, accountService.getEditForm(id));
        model.addAttribute("roles", Role.values());
        model.addAttribute("accountId", id);
        return "account/edit";
    }

    /**
     * Create a new staff account. Validates input, persists the account, then
     * redirects to the account list.
     */
    @PostMapping("/create")
    public String createAccount(@Valid @ModelAttribute(Attributes.FORM) AccountCreateForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        accountValidator.validateCreate(form, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "account/create";
        }

        accountService.createAccount(form);
        redirectAttributes.addFlashAttribute(Attributes.SUCCESS, "Account created successfully");
        return "redirect:/admin/accounts";
    }

    /**
     * Update an existing staff account. Validates input, persists changes, then
     * redirects to the account list.
     */
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
            model.addAttribute("roles", Role.values());
            model.addAttribute("accountId", id);
            return "account/edit";
        }

        accountService.updateAccount(id, form);
        redirectAttributes.addFlashAttribute(Attributes.SUCCESS, "Account updated successfully");
        return "redirect:/admin/accounts";
    }

    /**
     * Soft-delete a staff account. Only non-admin accounts can be deleted.
     */
    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            accountService.softDelete(id);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(Attributes.ERROR, e.getMessage());
            return "redirect:/admin/accounts/" + id;
        }
        redirectAttributes.addFlashAttribute(Attributes.SUCCESS, "Account deleted successfully");
        return "redirect:/admin/accounts";
    }
}
