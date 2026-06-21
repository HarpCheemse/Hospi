package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.guest.dto.BookingTrackForm;
import com.hospi.manage.features.guest.dto.OtpForm;
import com.hospi.manage.features.guest.dto.ReviewForm;
import com.hospi.manage.features.guest.service.BookingTrackerService;
import com.hospi.manage.features.reservation.entity.Reservation;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingTrackerControllerTest {

    @Mock
    private BookingTrackerService bookingTrackerService;
    @Mock
    private OtpService otpService;
    @Mock
    private EmailService emailService;
    @Mock
    private HttpSession session;
    @Mock
    private Model model;

    @InjectMocks
    private BookingTrackerController controller;

    @Test
    void myBooking_shouldShowTrackForm_whenNoTrackedCodes() {
        when(session.getAttribute("trackedBookingCodes")).thenReturn(null);

        String view = controller.myBooking(session, model);

        assertEquals("guest/my-booking", view);
        verify(model).addAttribute(eq("form"), any(BookingTrackForm.class));
    }

    @Test
    void myBooking_shouldShowReservations_whenTrackedCodesExist() {
        when(session.getAttribute("trackedBookingCodes")).thenReturn(Set.of("CODE"));
        when(bookingTrackerService.resolveByCodes(anySet())).thenReturn(java.util.List.of());
        when(bookingTrackerService.buildReviewMap(anyList())).thenReturn(java.util.Map.of());
        when(bookingTrackerService.buildPillClasses(anyList())).thenReturn(java.util.Map.of());

        String view = controller.myBooking(session, model);

        assertEquals("guest/my-booking", view);
        verify(model).addAttribute(eq("reservations"), anyList());
    }

    @Test
    void verifyPage_shouldRender_whenSessionHasEmail() {
        when(session.getAttribute("trackedEmail")).thenReturn("a@b.com");
        when(session.getAttribute("pendingCode")).thenReturn("CODE");

        String view = controller.verifyPage(null, session, model);

        assertEquals("guest/my-booking-verify", view);
        verify(model).addAttribute("verifyEmail", "a@b.com");
        verify(model).addAttribute(eq("otpForm"), any(OtpForm.class));
    }

    @Test
    void verifyPage_shouldRedirect_whenNoSession() {
        when(session.getAttribute("trackedEmail")).thenReturn(null);

        String view = controller.verifyPage(null, session, model);

        assertEquals("redirect:/my-booking", view);
    }

    @Test
    void lookup_shouldReturnForm_whenBindingErrors() {
        BookingTrackForm form = new BookingTrackForm();
        BindingResult binding = new BeanPropertyBindingResult(form, "form");
        binding.reject("error", "error");

        String view = controller.lookup(form, binding, session, mock(RedirectAttributes.class));

        assertEquals("guest/my-booking", view);
    }

    @Test
    void lookup_shouldRedirectWithError_whenBookingNotFound() {
        BookingTrackForm form = new BookingTrackForm();
        form.setEmail("a@b.com");
        form.setBookingCode("CODE");
        BindingResult binding = new BeanPropertyBindingResult(form, "form");
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        when(bookingTrackerService.lookupByEmailAndCode("a@b.com", "CODE")).thenReturn(Optional.empty());

        String view = controller.lookup(form, binding, session, redirect);

        assertEquals("redirect:/my-booking", view);
        assertNotNull(((RedirectAttributesModelMap) redirect).getFlashAttributes().get("error"));
    }

    @Test
    void lookup_shouldSendOtpAndRedirect_whenBookingFound() {
        BookingTrackForm form = new BookingTrackForm();
        form.setEmail("a@b.com");
        form.setBookingCode("CODE");
        BindingResult binding = new BeanPropertyBindingResult(form, "form");
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        when(bookingTrackerService.lookupByEmailAndCode("a@b.com", "CODE"))
                .thenReturn(Optional.of(new Reservation()));
        when(otpService.createOtp("a@b.com", OtpType.BOOKING_TRACK)).thenReturn("123456");

        String view = controller.lookup(form, binding, session, redirect);

        assertEquals("redirect:/my-booking/verify?email=a@b.com", view);
        verify(emailService).send(eq("a@b.com"), anyString(), anyString());
        verify(session).setAttribute("trackedEmail", "a@b.com");
        verify(session).setAttribute("pendingCode", "CODE");
    }

    @Test
    void verify_shouldRedirect_whenSessionMissing() {
        OtpForm form = new OtpForm();
        form.setOtp("123456");
        BindingResult binding = new BeanPropertyBindingResult(form, "otpForm");
        when(session.getAttribute("trackedEmail")).thenReturn(null);

        String view = controller.verify(form, binding, session, model, mock(RedirectAttributes.class));

        assertEquals("redirect:/my-booking", view);
    }

    @Test
    void verify_shouldReturnForm_whenOtpBlank() {
        OtpForm form = new OtpForm();
        form.setOtp("");
        BindingResult binding = new BeanPropertyBindingResult(form, "otpForm");
        binding.rejectValue("otp", "NotBlank", "OTP is required");
        when(session.getAttribute("trackedEmail")).thenReturn("a@b.com");
        when(session.getAttribute("pendingCode")).thenReturn("CODE");

        String view = controller.verify(form, binding, session, model, mock(RedirectAttributes.class));

        assertEquals("guest/my-booking-verify", view);
        verify(model).addAttribute("verifyEmail", "a@b.com");
    }

    @Test
    void verify_shouldRedirectWithError_whenOtpInvalid() {
        OtpForm form = new OtpForm();
        form.setOtp("wrong");
        BindingResult binding = new BeanPropertyBindingResult(form, "otpForm");
        when(session.getAttribute("trackedEmail")).thenReturn("a@b.com");
        when(session.getAttribute("pendingCode")).thenReturn("CODE");
        when(otpService.verifyOtp("a@b.com", "wrong", OtpType.BOOKING_TRACK)).thenReturn(false);
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String view = controller.verify(form, binding, session, model, redirect);

        assertEquals("redirect:/my-booking/verify?email=a@b.com", view);
        assertNotNull(((RedirectAttributesModelMap) redirect).getFlashAttributes().get("error"));
    }

    @Test
    void verify_shouldAddCodeToSession_whenOtpValid() {
        OtpForm form = new OtpForm();
        form.setOtp("123456");
        BindingResult binding = new BeanPropertyBindingResult(form, "otpForm");
        when(session.getAttribute("trackedEmail")).thenReturn("a@b.com");
        when(session.getAttribute("pendingCode")).thenReturn("CODE");
        when(otpService.verifyOtp("a@b.com", "123456", OtpType.BOOKING_TRACK)).thenReturn(true);
        Set<String> trackedCodes = new java.util.LinkedHashSet<>();
        when(session.getAttribute("trackedBookingCodes")).thenReturn(trackedCodes);
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String view = controller.verify(form, binding, session, model, redirect);

        assertEquals("redirect:/my-booking", view);
        assertTrue(trackedCodes.contains("CODE"));
        assertNotNull(((RedirectAttributesModelMap) redirect).getFlashAttributes().get("success"));
    }

    @Test
    void clear_shouldRemoveSessionAttributes() {
        String view = controller.clear(session);

        assertEquals("redirect:/my-booking", view);
        verify(session).removeAttribute("trackedBookingCodes");
        verify(session).removeAttribute("trackedEmail");
        verify(session).removeAttribute("pendingCode");
    }

    @Test
    void submitReview_shouldRedirect_whenBindingErrors() {
        ReviewForm form = new ReviewForm();
        BindingResult binding = new BeanPropertyBindingResult(form, "review");
        binding.reject("error", "error");

        String view = controller.submitReview(form, binding, mock(RedirectAttributes.class));

        assertEquals("redirect:/my-booking", view);
    }

    @Test
    void submitReview_shouldRedirectWithSuccess_whenValid() {
        ReviewForm form = new ReviewForm();
        form.setReservationId(1L);
        form.setRating(4);
        BindingResult binding = new BeanPropertyBindingResult(form, "review");
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String view = controller.submitReview(form, binding, redirect);

        assertEquals("redirect:/my-booking", view);
        assertNotNull(((RedirectAttributesModelMap) redirect).getFlashAttributes().get("success"));
    }
}
