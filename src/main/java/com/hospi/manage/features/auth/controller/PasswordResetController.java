package com.hospi.manage.features.auth.controller;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.repository.AccountRepository;
import com.hospi.manage.features.account.validator.AccountValidator;
import com.hospi.manage.features.auth.dto.request.ForgotPasswordForm;
import com.hospi.manage.features.auth.dto.request.ResetPasswordForm;
import com.hospi.manage.features.auth.dto.request.VerifyOtpForm;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.hospi.manage.common.constant.Attributes.FORM;
import static com.hospi.manage.common.constant.Attributes.SUCCESS;

/**
 * Controller for the password reset flow (forgot → verify OTP → reset).
 */
@Controller
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {
    private final AccountRepository accountRepository;
    private final OtpService otpService;
    private final AccountValidator accountValidator;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    /**
     * Show the forgot-password form where the user enters their email.
     */
    @GetMapping("/forgot")
    public String forgot(Model model) {
        model.addAttribute(FORM,
                new ForgotPasswordForm(null));
        return "auth/forgot-password";
    }

    /**
     * Process the forgot-password request. Validate the email, create an OTP, send
     * it via email, and redirect to the OTP verification page.
     */
    @PostMapping("/forgot")
    public String handleForgotPassword(@Valid @ModelAttribute(FORM) ForgotPasswordForm form,
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

        emailService.send(form.email(),
                "Password Reset",
                "Your OTP code is: " + otp);

        redirectAttributes.addFlashAttribute(SUCCESS,
                "Email sent successfully to " + emailService.maskEmail(form.email()));
        redirectAttributes.addAttribute("email",
                form.email());
        return "redirect:/auth/password/verify-otp";
    }

    /**
     * Show the OTP verification page for a given email.
     */
    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam String email, Model model) {
        model.addAttribute(FORM,
                new VerifyOtpForm(email,
                        null));
        return "auth/verify-otp";
    }

    /**
     * Verify the OTP submitted by the user. On success, issue a reset token and
     * redirect to the password reset page.
     */
    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute(FORM) VerifyOtpForm form, BindingResult bindingResult,
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

    /**
     * Show the password reset form. Redirect to forgot-password if the token is
     * invalid or expired.
     */
    @GetMapping("/reset")
    public String showResetPassword(@RequestParam String token, Model model) {
        if (!otpService.isValidToken(token)) {
            return "redirect:/auth/password/forgot";
        }

        model.addAttribute(FORM,
                new ResetPasswordForm(null,
                        null));
        model.addAttribute("token",
                token);
        return "auth/password-reset";
    }

    /**
     * Process the password reset. Validate the new password, update the account,
     * invalidate the token, and redirect to login.
     */
    @PostMapping("/reset")
    public String resetPassword(@RequestParam String token, @Valid @ModelAttribute(FORM) ResetPasswordForm form,
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

        redirectAttributes.addFlashAttribute(SUCCESS,
                "Password reset successfully");
        return "redirect:/login";
    }
}
