package com.hospi.manage.features.auth.controller;

import com.hospi.manage.features.admin.account.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the staff login page.
 */
@Controller
@RequestMapping("/login")
public class LoginController {
    private final AccountService accountService;

    LoginController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Show the login page. Display error or logout message if present in request
     * params.
     */
    @GetMapping
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
//        accountService.rehashAllPasswords("123");

        if (error != null) {
            model.addAttribute("error",
                    "Invalid email or password");
        }

        if (logout != null) {
            model.addAttribute("message",
                    "You have been logged out");
        }

        return "auth/login";
    }
}