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

import java.util.*;

import static com.hospi.manage.common.constant.Attributes.*;
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
        when(session.getAttribute(TRACKED_BOOKING_CODES)).thenReturn(null);

        String view = controller.myBooking(session, model);

        assertEquals("guest/my-booking", view);
        verify(model).addAttribute(eq(FORM), any(BookingTrackForm.class));
    }

    @Test
    void myBooking_shouldShowReservations_whenTrackedCodesExist() {
        when(session.getAttribute(TRACKED_BOOKING_CODES)).thenReturn(Set.of("CODE"));
        when(bookingTrackerService.resolveByCodes(anySet())).thenReturn(List.of());
        when(bookingTrackerService.buildReviewMap(anyList())).thenReturn(Map.of());
        when(bookingTrackerService.buildPillClasses(anyList())).thenReturn(Map.of());

        String view = controller.myBooking(session, model);

        assertEquals("guest/my-booking", view);
        verify(model).addAttribute(eq(RESERVATIONS), anyList());
    }

    @Test
    void verifyPage_shouldRender_whenSessionHasEmail() {
        when(session.getAttribute(TRACKED_EMAIL)).thenReturn("a@b.com");
        when(session.getAttribute(PENDING_CODE)).thenReturn("CODE");

        String view = controller.verifyPage(null, session, model);

        assertEquals("guest/my-booking-verify", view);
        verify(model).addAttribute(VERIFY_EMAIL, "a@b.com");
        verify(model).addAttribute(eq(OTP_FORM), any(OtpForm.class));
    }

    @Test
    void verifyPage_shouldRedirect_whenNoSession() {
        when(session.getAttribute(TRACKED_EMAIL)).thenReturn(null);

        String view = controller.verifyPage(null, session, model);

        assertEquals("redirect:/my-booking", view);
    }

    @Test
    void lookup_shouldReturnForm_whenBindingErrors() {
        BookingTrackForm form = new BookingTrackForm("a@b.com", "CODE");
        BindingResult binding = new BeanPropertyBindingResult(form, FORM);
        binding.reject("error", "error");

        String view = controller.lookup(form, binding, session, mock(RedirectAttributes.class));

        assertEquals("guest/my-booking", view);
    }

    @Test
    void lookup_shouldRedirectWithError_whenBookingNotFound() {
        BookingTrackForm form = new BookingTrackForm("a@b.com", "CODE");
        BindingResult binding = new BeanPropertyBindingResult(form, FORM);
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        when(bookingTrackerService.lookupByEmailAndCode("a@b.com", "CODE"))
                .thenThrow(new com.hospi.manage.common.exception.ResourceNotFoundException("Reservation"));

        String view = controller.lookup(form, binding, session, redirect);

        assertEquals("redirect:/my-booking", view);
        assertNotNull(((RedirectAttributesModelMap) redirect).getFlashAttributes().get(ERROR));
    }

    @Test
    void lookup_shouldSendOtpAndRedirect_whenBookingFound() {
        BookingTrackForm form = new BookingTrackForm("a@b.com", "CODE");
        BindingResult binding = new BeanPropertyBindingResult(form, FORM);
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        when(bookingTrackerService.lookupByEmailAndCode("a@b.com", "CODE"))
                .thenReturn(new Reservation());
        when(otpService.createOtp("a@b.com", OtpType.BOOKING_TRACK)).thenReturn("123456");

        String view = controller.lookup(form, binding, session, redirect);

        assertTrue(view.startsWith("redirect:"));
        assertTrue(view.contains("/my-booking/verify"));
        assertTrue(view.contains("email=a@b.com"));
        verify(emailService).send(eq("a@b.com"), anyString(), anyString());
        verify(session).setAttribute(TRACKED_EMAIL, "a@b.com");
        verify(session).setAttribute(PENDING_CODE, "CODE");
    }

    @Test
    void verify_shouldRedirect_whenSessionMissing() {
        OtpForm form = new OtpForm("123456");
        BindingResult binding = new BeanPropertyBindingResult(form, OTP_FORM);
        when(session.getAttribute(TRACKED_EMAIL)).thenReturn(null);

        String view = controller.verify(form, binding, session, model, mock(RedirectAttributes.class));

        assertEquals("redirect:/my-booking", view);
    }

    @Test
    void verify_shouldReturnForm_whenOtpBlank() {
        OtpForm form = new OtpForm("");
        BindingResult binding = new BeanPropertyBindingResult(form, OTP_FORM);
        binding.rejectValue("otp", "NotBlank", "OTP is required");
        when(session.getAttribute(TRACKED_EMAIL)).thenReturn("a@b.com");
        when(session.getAttribute(PENDING_CODE)).thenReturn("CODE");

        String view = controller.verify(form, binding, session, model, mock(RedirectAttributes.class));

        assertEquals("guest/my-booking-verify", view);
        verify(model).addAttribute(VERIFY_EMAIL, "a@b.com");
    }

    @Test
    void verify_shouldRedirectWithError_whenOtpInvalid() {
        OtpForm form = new OtpForm("wrong");
        BindingResult binding = new BeanPropertyBindingResult(form, OTP_FORM);
        when(session.getAttribute(TRACKED_EMAIL)).thenReturn("a@b.com");
        when(session.getAttribute(PENDING_CODE)).thenReturn("CODE");
        when(otpService.verifyOtp("a@b.com", "wrong", OtpType.BOOKING_TRACK)).thenReturn(false);
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String view = controller.verify(form, binding, session, model, redirect);

        assertTrue(view.startsWith("redirect:"));
        assertTrue(view.contains("/my-booking/verify"));
        assertTrue(view.contains("email=a@b.com"));
        assertNotNull(((RedirectAttributesModelMap) redirect).getFlashAttributes().get(ERROR));
    }

    @Test
    void verify_shouldAddCodeToSession_whenOtpValid() {
        OtpForm form = new OtpForm("123456");
        BindingResult binding = new BeanPropertyBindingResult(form, OTP_FORM);
        when(session.getAttribute(TRACKED_EMAIL)).thenReturn("a@b.com");
        when(session.getAttribute(PENDING_CODE)).thenReturn("CODE");
        when(otpService.verifyOtp("a@b.com", "123456", OtpType.BOOKING_TRACK)).thenReturn(true);
        Set<String> trackedCodes = new LinkedHashSet<>();
        when(session.getAttribute(TRACKED_BOOKING_CODES)).thenReturn(trackedCodes);
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String view = controller.verify(form, binding, session, model, redirect);

        assertEquals("redirect:/my-booking", view);
        assertTrue(trackedCodes.contains("CODE"));
        assertNotNull(((RedirectAttributesModelMap) redirect).getFlashAttributes().get(SUCCESS));
    }

    @Test
    void clear_shouldRemoveSessionAttributes() {
        String view = controller.clear(session);

        assertEquals("redirect:/my-booking", view);
        verify(session).removeAttribute(TRACKED_BOOKING_CODES);
        verify(session).removeAttribute(TRACKED_EMAIL);
        verify(session).removeAttribute(PENDING_CODE);
    }

    @Test
    void submitReview_shouldRedirect_whenSessionEmpty() {
        ReviewForm form = new ReviewForm(null, null);
        BindingResult binding = new BeanPropertyBindingResult(form, "review");
        when(session.getAttribute(TRACKED_BOOKING_CODES)).thenReturn(new LinkedHashSet<>());

        String view = controller.submitReview(form, binding, session, model);

        assertEquals("guest/my-booking", view);
    }

    @Test
    void submitReview_shouldSaveAndRedirect_whenValid() {
        ReviewForm form = new ReviewForm(4, 1L);
        BindingResult binding = new BeanPropertyBindingResult(form, "review");
        Set<String> trackedCodes = new LinkedHashSet<>(Set.of("CODE"));
        when(session.getAttribute(TRACKED_BOOKING_CODES)).thenReturn(trackedCodes);

        String view = controller.submitReview(form, binding, session, model);

        assertEquals("redirect:/my-booking", view);
        verify(bookingTrackerService).submitReview(1L, 4);
    }
}
