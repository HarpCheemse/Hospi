package com.hospi.manage.features.auth.controller;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.admin.account.entity.Account;
import com.hospi.manage.features.admin.account.repository.AccountRepository;
import com.hospi.manage.features.admin.account.validator.AccountValidator;
import com.hospi.manage.features.auth.dto.ForgotPasswordForm;
import com.hospi.manage.features.auth.dto.ResetPasswordForm;
import com.hospi.manage.features.auth.dto.VerifyOtpForm;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth/password")
public class PasswordResetController {
    private final AccountRepository accountRepository;
    private final OtpService otpService;
    private final AccountValidator accountValidator;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    public PasswordResetController(AccountRepository accountRepository, OtpService otpService,
                                   AccountValidator accountValidator, PasswordEncoder encoder,
                                   EmailService emailService) {
        this.accountRepository = accountRepository;
        this.otpService = otpService;
        this.accountValidator = accountValidator;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    @GetMapping("/forgot")
    public String forgot(Model model) {
        model.addAttribute("form",
                new ForgotPasswordForm(null));
        return "auth/forgot-password";
    }

    @PostMapping("/forgot")
    public String handleForgotPassword(@Valid @ModelAttribute("form") ForgotPasswordForm form,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        if (!accountRepository.existsByEmail(form.email())) {
            bindingResult.rejectValue("email",
                    "notFound",
                    "No account found with this email");
            return "auth/forgot-password";
        }

        String otp = otpService.createOtp(form.email(),
                OtpType.PASSWORD_RESET);

        System.out.println("OTP CODE: " + otp);
        emailService.send(form.email(),
                "Password Reset",
                "Your OTP code is: " + otp);

        redirectAttributes.addAttribute("email",
                form.email());
        return "redirect:/auth/password/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam String email, Model model) {
        model.addAttribute("form",
                new VerifyOtpForm(email,
                        null));
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute("form") VerifyOtpForm form, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/verify-otp";
        }

        boolean verified = otpService.verifyOtp(form.email(),
                form.otp(),
                OtpType.PASSWORD_RESET);

        if (!verified) {
            bindingResult.rejectValue("otp",
                    "invalid",
                    "Invalid or expired OTP");
            return "auth/verify-otp";
        }

        String token = otpService.issueToken(form.email(),
                OtpType.PASSWORD_RESET);
        redirectAttributes.addAttribute("token",
                token);
        return "redirect:/auth/password/reset";
    }

    @GetMapping("/reset")
    public String showResetPassword(@RequestParam String token, Model model) {
        if (!otpService.isValidToken(token)) {
            return "redirect:/auth/password/forgot";
        }

        model.addAttribute("form",
                new ResetPasswordForm(null,
                        null));
        model.addAttribute("token",
                token);
        return "auth/password-reset";
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam String token, @Valid @ModelAttribute("form") ResetPasswordForm form,
                                BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (!otpService.isValidToken(token)) {
            return "redirect:/auth/password/forgot";
        }

        accountValidator.validateResetPassword(form,
                bindingResult);
        if (bindingResult.hasErrors()) {
            return "auth/password-reset";
        }

        String email = otpService.getEmailByToken(token);
        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Account"));

        account.setPasswordHash(encoder.encode(form.newPassword()));
        accountRepository.save(account);

        otpService.invalidateToken(token);

        redirectAttributes.addFlashAttribute("success",
                "Password reset successfully");
        return "redirect:/login";
    }
}