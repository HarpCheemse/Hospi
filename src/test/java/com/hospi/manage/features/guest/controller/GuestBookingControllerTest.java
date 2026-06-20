package com.hospi.manage.features.guest.controller;

import com.hospi.manage.features.admin.config.entity.SystemConfig;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.guest.validation.BookingDateValidator;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.room.dto.response.RoomTypeAvailability;
import com.hospi.manage.features.room.entity.RoomType;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestBookingControllerTest {

    @Mock
    private ReservationService reservationService;
    @Mock
    private RoomAvailabilityService roomAvailabilityService;
    @Mock
    private SystemConfigService systemConfigService;
    @Mock
    private OtpService otpService;
    @Mock
    private EmailService emailService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private BookingDateValidator bookingDateValidator;

    @InjectMocks
    private GuestBookingController controller;

    private HttpSession sessionWithDraft(BookingDraft draft) {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("bookingDraft")).thenReturn(draft);
        return session;
    }

    private BookingDraft draftWithDates() {
        BookingDraft draft = new BookingDraft();
        draft.setCheckInAt(LocalDate.now().plusDays(5));
        draft.setCheckOutAt(LocalDate.now().plusDays(10));
        return draft;
    }

    private RoomTypeAvailability mockAvailability(Long id, String name, BigDecimal price, int available) {
        RoomType roomType = mock(RoomType.class);
        lenient().when(roomType.getId()).thenReturn(id);
        lenient().when(roomType.getName()).thenReturn(name);
        lenient().when(roomType.getBasePrice()).thenReturn(price);
        lenient().when(roomType.getMaxOccupancy()).thenReturn(2);
        lenient().when(roomType.getArea()).thenReturn(32);
        lenient().when(roomType.getBedType()).thenReturn(null);
        lenient().when(roomType.getFeatures()).thenReturn("");
        lenient().when(roomType.getActive()).thenReturn(true);
        lenient().when(roomType.getPictures()).thenReturn(List.of());
        return new RoomTypeAvailability(roomType, 5, available);
    }

    private SystemConfig config(int maxRooms, BigDecimal depositPct) {
        SystemConfig config = new SystemConfig();
        config.setMaximumRoomPerBook(maxRooms);
        config.setDefaultDepositPercentage(depositPct);
        return config;
    }

    @Test
    void submitRooms_shouldRedirectToBook_whenDraftDatesNull() {
        HttpSession session = sessionWithDraft(new BookingDraft());
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        String view = controller.submitRooms(null, null, session, redirect);

        assertEquals("redirect:/book", view);
    }

    @Test
    void submitRooms_shouldRedirectToRooms_whenParamsNull() {
        HttpSession session = sessionWithDraft(draftWithDates());
        RedirectAttributes redirect = new RedirectAttributesModelMap();
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of());

        String view = controller.submitRooms(null, null, session, redirect);

        assertEquals("redirect:/book/rooms", view);
    }

    @Test
    void submitRooms_shouldRedirectToRooms_whenAllCountsZero() {
        HttpSession session = sessionWithDraft(draftWithDates());
        RedirectAttributes redirect = new RedirectAttributesModelMap();
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of());

        String view = controller.submitRooms(List.of(1L), List.of(0), session, redirect);

        assertEquals("redirect:/book/rooms", view);
        assertNotNull(((RedirectAttributesModelMap) redirect).getFlashAttributes().get("error"));
    }

    @Test
    void submitRooms_shouldRedirectToRooms_whenExceedsMaxRooms() {
        HttpSession session = sessionWithDraft(draftWithDates());
        RedirectAttributes redirect = new RedirectAttributesModelMap();
        RoomTypeAvailability rta = mockAvailability(1L, "Deluxe", BigDecimal.valueOf(150), 10);
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of(rta));
        when(systemConfigService.getConfig()).thenReturn(config(5, BigDecimal.valueOf(20)));

        String view = controller.submitRooms(List.of(1L), List.of(6), session, redirect);

        assertEquals("redirect:/book/rooms", view);
        String error = (String) ((RedirectAttributesModelMap) redirect).getFlashAttributes().get("error");
        assertNotNull(error);
        assertTrue(error.contains("exceeds"));
    }

    @Test
    void submitRooms_shouldRedirectToVerify_whenValid() {
        BookingDraft draft = draftWithDates();
        HttpSession session = sessionWithDraft(draft);
        RedirectAttributes redirect = new RedirectAttributesModelMap();
        RoomTypeAvailability rta = mockAvailability(1L, "Deluxe", BigDecimal.valueOf(150), 10);
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of(rta));
        when(systemConfigService.getConfig()).thenReturn(config(5, BigDecimal.valueOf(20)));

        String view = controller.submitRooms(List.of(1L), List.of(2), session, redirect);

        assertEquals("redirect:/book/verify", view);
        assertNotNull(draft.getRoomSelections());
    }

    @Test
    void submitRooms_shouldSetCorrectTotals_whenValid() {
        BookingDraft draft = draftWithDates();
        HttpSession session = sessionWithDraft(draft);
        RedirectAttributes redirect = new RedirectAttributesModelMap();
        RoomTypeAvailability rta = mockAvailability(1L, "Deluxe", BigDecimal.valueOf(150), 10);
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of(rta));
        when(systemConfigService.getConfig()).thenReturn(config(5, BigDecimal.valueOf(20)));

        controller.submitRooms(List.of(1L), List.of(2), session, redirect);

        assertEquals(1, draft.getRoomSelections().size());
        assertEquals(2, draft.getRoomSelections().get(0).count());
        assertEquals(BigDecimal.valueOf(300).stripTrailingZeros(),
                draft.getTotalPrice().stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(60).stripTrailingZeros(),
                draft.getDepositAmount().stripTrailingZeros());
    }

    @Test
    void submitRooms_shouldHandleMultipleRoomTypes() {
        BookingDraft draft = draftWithDates();
        HttpSession session = sessionWithDraft(draft);
        RedirectAttributes redirect = new RedirectAttributesModelMap();
        RoomTypeAvailability deluxe = mockAvailability(1L, "Deluxe", BigDecimal.valueOf(150), 10);
        RoomTypeAvailability suite = mockAvailability(2L, "Suite", BigDecimal.valueOf(300), 5);
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of(deluxe, suite));
        when(systemConfigService.getConfig()).thenReturn(config(10, BigDecimal.valueOf(20)));

        controller.submitRooms(List.of(1L, 2L), List.of(2, 1), session, redirect);

        assertEquals(2, draft.getRoomSelections().size());
        assertEquals(BigDecimal.valueOf(600).stripTrailingZeros(),
                draft.getTotalPrice().stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(120).stripTrailingZeros(),
                draft.getDepositAmount().stripTrailingZeros());
    }
}
