package com.hospi.manage.features.auth.controller;

import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.repository.AccountRepository;
import com.hospi.manage.features.account.validator.AccountValidator;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.util.Optional;

import static com.hospi.manage.common.constant.Attributes.SUCCESS;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private AccountValidator accountValidator;

    @MockitoBean
    private PasswordEncoder encoder;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void forgot_shouldRender() throws Exception {
        mockMvc.perform(get("/auth/password/forgot"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"))
                .andExpect(model().attributeExists("form"))
                .andExpect(content().string(containsString("Forgot Password")));
    }

    @Test
    void handleForgotPassword_shouldRedirect_onSuccess() throws Exception {
        when(accountRepository.existsByEmail("test@test.com")).thenReturn(true);
        when(otpService.createOtp("test@test.com", OtpType.PASSWORD_RESET)).thenReturn("123456");
        when(emailService.maskEmail("test@test.com")).thenReturn("t***@t.com");

        mockMvc.perform(post("/auth/password/forgot")
                        .param("email", "test@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/auth/password/verify-otp?email=*"))
                .andExpect(flash().attributeExists(SUCCESS));
    }

    @Test
    void handleForgotPassword_shouldReRender_onValidationError() throws Exception {
        mockMvc.perform(post("/auth/password/forgot")
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"));
    }

    @Test
    void handleForgotPassword_shouldRedirect_whenEmailNotFound() throws Exception {
        when(accountRepository.existsByEmail("test@test.com")).thenReturn(false);

        mockMvc.perform(post("/auth/password/forgot")
                        .param("email", "test@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/password/forgot"))
                .andExpect(flash().attributeExists(SUCCESS));
    }

    @Test
    void verifyOtpPage_shouldRender() throws Exception {
        mockMvc.perform(get("/auth/password/verify-otp")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify-otp"))
                .andExpect(model().attributeExists("form"))
                .andExpect(content().string(containsString("Verify OTP")));
    }

    @Test
    void verifyOtp_shouldRedirect_onSuccess() throws Exception {
        when(otpService.verifyOtp("test@test.com", "123456", OtpType.PASSWORD_RESET)).thenReturn(true);
        when(otpService.issueToken("test@test.com", OtpType.PASSWORD_RESET)).thenReturn("token-123");

        mockMvc.perform(post("/auth/password/verify-otp")
                        .param("email", "test@test.com")
                        .param("otp", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/password/reset"));
    }

    @Test
    void verifyOtp_shouldReRender_onValidationError() throws Exception {
        mockMvc.perform(post("/auth/password/verify-otp")
                        .param("email", "test@test.com")
                        .param("otp", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify-otp"));
    }

    @Test
    void verifyOtp_shouldReRender_whenOtpInvalid() throws Exception {
        when(otpService.verifyOtp("test@test.com", "123456", OtpType.PASSWORD_RESET)).thenReturn(false);

        mockMvc.perform(post("/auth/password/verify-otp")
                        .param("email", "test@test.com")
                        .param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify-otp"));
    }

    @Test
    void showResetPassword_shouldRender_whenTokenValid() throws Exception {
        when(otpService.isValidToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/auth/password/reset")
                        .sessionAttr("resetToken", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/password-reset"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attribute("token", "valid-token"))
                .andExpect(content().string(containsString("Reset Password")));
    }

    @Test
    void showResetPassword_shouldRedirect_whenTokenInvalid() throws Exception {
        when(otpService.isValidToken("bad-token")).thenReturn(false);

        mockMvc.perform(get("/auth/password/reset")
                        .sessionAttr("resetToken", "bad-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/password/forgot"));
    }

    @Test
    void showResetPassword_shouldRedirect_whenNoTokenInSession() throws Exception {
        mockMvc.perform(get("/auth/password/reset"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/password/forgot"));
    }

    @Test
    void resetPassword_shouldRedirect_onSuccess() throws Exception {
        when(otpService.isValidToken("valid-token")).thenReturn(true);
        when(otpService.getEmailByToken("valid-token")).thenReturn("test@test.com");
        var account = mock(Account.class);
        when(accountRepository.findByEmail("test@test.com")).thenReturn(Optional.of(account));
        when(encoder.encode("newPassword123")).thenReturn("encoded-hash");

        mockMvc.perform(post("/auth/password/reset")
                        .sessionAttr("resetToken", "valid-token")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void resetPassword_shouldRedirect_whenTokenInvalid() throws Exception {
        when(otpService.isValidToken("bad-token")).thenReturn(false);

        mockMvc.perform(post("/auth/password/reset")
                        .sessionAttr("resetToken", "bad-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/password/forgot"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void resetPassword_shouldRedirect_whenNoTokenInSession() throws Exception {
        mockMvc.perform(post("/auth/password/reset")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/password/forgot"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void resetPassword_shouldReRender_onValidationError() throws Exception {
        when(otpService.isValidToken("valid-token")).thenReturn(true);
        doAnswer(invocation -> {
            BindingResult br = invocation.getArgument(1);
            br.rejectValue("newPassword", "invalid", "Password must be at least 6 characters");
            return null;
        }).when(accountValidator).validateResetPassword(any(), any());

        mockMvc.perform(post("/auth/password/reset")
                        .sessionAttr("resetToken", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/password-reset"));
    }
}
