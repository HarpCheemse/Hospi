package com.hospi.manage.features.admin.account.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.admin.account.dto.AccountCreateForm;
import com.hospi.manage.features.admin.account.enums.AccountStatus;
import com.hospi.manage.features.admin.account.enums.Role;
import com.hospi.manage.features.admin.account.service.AccountService;
import com.hospi.manage.features.admin.account.validator.AccountValidator;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("statuses", AccountStatus.values());
        model.addAttribute("roles", Role.values());
        model.addAttribute(Attributes.FORM, form);
        return "admin/account/create";
    }

    @PostMapping("/create")
    public String createAccount(
            @Valid @ModelAttribute(Attributes.FORM) AccountCreateForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        accountValidator.validateCreate(form, bindingResult);

        System.out.println(bindingResult.getAllErrors());

        if (bindingResult.hasErrors()) {

            model.addAttribute("roles", Role.values());
            model.addAttribute("statuses", AccountStatus.values());

            return "admin/account/create";
        }

        accountService.createAccount(form);

        redirectAttributes.addFlashAttribute(
                Attributes.SUCCESS,
                "Account created successfully"
        );

        return "redirect:/admin/accounts";
    }
}
