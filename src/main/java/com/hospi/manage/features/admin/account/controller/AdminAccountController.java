package com.hospi.manage.features.admin.account.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.admin.account.dto.AccountCreateForm;
import com.hospi.manage.features.admin.account.dto.AccountEditForm;
import com.hospi.manage.features.admin.account.dto.AccountView;
import com.hospi.manage.features.admin.account.enums.AccountStatus;
import com.hospi.manage.features.admin.account.enums.Role;
import com.hospi.manage.features.admin.account.service.AccountService;
import com.hospi.manage.features.admin.account.validator.AccountValidator;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/admin/accounts")
public class AdminAccountController {
    private final AccountService accountService;
    private final AccountValidator accountValidator;

    AdminAccountController(AccountService accountService, AccountValidator accountValidator) {
        this.accountService = accountService;
        this.accountValidator = accountValidator;
    }


    @GetMapping
    public String list(Model model) {

        model.addAttribute(
                "accounts",
                accountService.findAllViews()
        );

        return "admin/account/list";
    }

    @GetMapping("/create")
    String create(Model model) {
        AccountCreateForm form = new AccountCreateForm(
                null,
                null,
                null,
                null,
                null
        );
        model.addAttribute("statuses",
                AccountStatus.values());
        model.addAttribute("roles",
                Role.values());
        model.addAttribute(Attributes.FORM,
                form);
        return "admin/account/create";
    }

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, Model model) {
        AccountView account = accountService.getAccountView(id);
        model.addAttribute("account",
                account);

        return "admin/account/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       Model model) {

        model.addAttribute(
                Attributes.FORM,
                accountService.getEditForm(id)
        );

        model.addAttribute("roles",
                Arrays.stream(Role.values())
                        .filter(role -> role != Role.ADMIN)
                        .toList());

        model.addAttribute("statuses",
                AccountStatus.values());

        model.addAttribute("accountId",
                id);

        return "admin/account/edit";
    }

    @PostMapping("/create")
    public String createAccount(
            @Valid @ModelAttribute(Attributes.FORM) AccountCreateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        accountValidator.validateCreate(form,
                bindingResult);

        if (bindingResult.hasErrors()) {

            model.addAttribute("roles",
                    Role.values());
            model.addAttribute("statuses",
                    AccountStatus.values());

            return "admin/account/create";
        }

        accountService.createAccount(form);

        redirectAttributes.addFlashAttribute(
                Attributes.SUCCESS,
                "Account created successfully"
        );

        return "redirect:/admin/accounts";
    }

    @PostMapping("/{id}/edit")
    public String updateAccount(@PathVariable Long id,
                                @Valid @ModelAttribute(Attributes.FORM) AccountEditForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        accountValidator.validateUpdate(id,
                form,
                bindingResult);

        if (bindingResult.hasErrors()) {

            model.addAttribute("roles",
                    Arrays.stream(Role.values())
                            .filter(role -> role != Role.ADMIN)
                            .toList());

            model.addAttribute("statuses",
                    AccountStatus.values());
            model.addAttribute("accountId",
                    id);

            return "admin/account/edit";
        }

        accountService.updateAccount(id,
                form);

        redirectAttributes.addFlashAttribute(
                Attributes.SUCCESS,
                "Account updated successfully"
        );

        return "redirect:/admin/accounts";
    }
}
