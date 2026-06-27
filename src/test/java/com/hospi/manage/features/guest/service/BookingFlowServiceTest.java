package com.hospi.manage.features.guest.service;

import com.hospi.manage.common.interfaces.EmailService;
import com.hospi.manage.features.config.entity.SystemConfig;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.features.auth.enums.OtpType;
import com.hospi.manage.features.auth.service.OtpService;
import com.hospi.manage.features.guest.dto.BookingDraft;
import com.hospi.manage.features.payment.service.PaymentService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.service.ReservationService;
import com.hospi.manage.features.reservation.service.RoomAvailabilityService;
import com.hospi.manage.features.room.dto.response.RoomTypeAvailability;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.BedType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingFlowServiceTest {

    @Mock
    private RoomAvailabilityService roomAvailabilityService;

    @Mock
    private SystemConfigService systemConfigService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingFlowService service;

    private final LocalDate checkIn = LocalDate.of(2026, 7, 1);
    private final LocalDate checkOut = LocalDate.of(2026, 7, 4);

    private BookingDraft draftWithDates() {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(checkIn, checkOut));
        return draft;
    }

    private RoomTypeAvailability availability(Long id, String name, BigDecimal price, int total, int available) {
        RoomType roomType = new RoomType();
        roomType.setId(id);
        roomType.setName(name);
        roomType.setMaxOccupancy(2);
        roomType.setBasePrice(price);
        roomType.setBedType(BedType.QUEEN);
        roomType.setArea(30);
        roomType.setFeatures("WiFi, TV");
        roomType.setActive(true);
        return new RoomTypeAvailability(roomType, total, available);
    }

    // --- buildAvailabilityView ---

    @Test
    void buildAvailabilityView_shouldReturnPopulatedView() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(checkIn, checkOut))
                .thenReturn(List.of(
                        availability(1L, "Deluxe", new BigDecimal("200"), 5, 3),
                        availability(2L, "Suite", new BigDecimal("400"), 3, 2)
                ));

        SystemConfig config = new SystemConfig();
        config.setMaximumRoomPerBook(10);
        config.setDefaultDepositPercentage(new BigDecimal("20"));

        when(systemConfigService.getConfig()).thenReturn(config);

        BookingFlowService.AvailabilityView result = service.buildAvailabilityView(draft);

        assertEquals(2, result.availability().size());
        assertEquals(Long.valueOf(1L), result.availability().get(0).roomTypeId());
        assertEquals("Deluxe", result.availability().get(0).name());
        assertEquals(3, result.availability().get(0).availableRooms());
        assertEquals(new BigDecimal("200"), result.availability().get(0).basePrice());
        assertEquals(Integer.valueOf(10), result.maxRooms());
        assertEquals(new BigDecimal("20"), result.depositPercentage());
        assertEquals(3L, result.nights());
        verify(roomAvailabilityService).getAvailability(checkIn, checkOut);
        verify(systemConfigService).getConfig();
    }

    @Test
    void buildAvailabilityView_shouldComputeZeroNights_whenSameDay() {
        var draft = new BookingDraft();
        draft.setDates(new BookingDraft.BookingDates(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1)));
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of());
        when(systemConfigService.getConfig()).thenReturn(new SystemConfig());

        BookingFlowService.AvailabilityView result = service.buildAvailabilityView(draft);

        assertEquals(0L, result.nights());
    }

    // --- processRoomSelections ---

    @Test
    void processRoomSelections_shouldSetRoomsOnDraft_whenValid() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(checkIn, checkOut))
                .thenReturn(List.of(
                        availability(1L, "Deluxe", new BigDecimal("200"), 5, 3)
                ));

        SystemConfig config = new SystemConfig();
        config.setMaximumRoomPerBook(10);
        config.setDefaultDepositPercentage(new BigDecimal("25"));

        when(systemConfigService.getConfig()).thenReturn(config);

        service.processRoomSelections(draft, List.of(1L), List.of(2));

        BookingDraft.BookingRooms rooms = draft.getRooms();
        assertNotNull(rooms);
        assertEquals(1, rooms.selections().size());
        assertEquals(Long.valueOf(1L), rooms.selections().get(0).roomTypeId());
        assertEquals("Deluxe", rooms.selections().get(0).roomTypeName());
        assertEquals(2, rooms.selections().get(0).count());
        assertEquals(new BigDecimal("200"), rooms.selections().get(0).basePrice());
        assertEquals(new BigDecimal("1200"), rooms.totalPrice());
        assertEquals(new BigDecimal("300"), rooms.depositAmount());
        assertEquals(3L, rooms.numberOfNights());
    }

    @Test
    void processRoomSelections_shouldThrow_whenRoomTypeIdsNull() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.processRoomSelections(draft, null, List.of(1)));
    }

    @Test
    void processRoomSelections_shouldThrow_whenCountsNull() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.processRoomSelections(draft, List.of(1L), null));
    }

    @Test
    void processRoomSelections_shouldThrow_whenSizesMismatched() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.processRoomSelections(draft, List.of(1L, 2L), List.of(1)));
    }

    @Test
    void processRoomSelections_shouldThrow_whenAllCountsZero() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(any(), any()))
                .thenReturn(List.of(
                        availability(1L, "Deluxe", new BigDecimal("200"), 5, 3)
                ));

        assertThrows(IllegalArgumentException.class,
                () -> service.processRoomSelections(draft, List.of(1L), List.of(0)));
    }

    @Test
    void processRoomSelections_shouldThrow_whenRoomTypeNotInAvailability() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(any(), any())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.processRoomSelections(draft, List.of(999L), List.of(2)));
    }

    @Test
    void processRoomSelections_shouldThrow_whenCountExceedsAvailable() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(checkIn, checkOut))
                .thenReturn(List.of(
                        availability(1L, "Deluxe", new BigDecimal("200"), 5, 3)
                ));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.processRoomSelections(draft, List.of(1L), List.of(5)));
        assertTrue(ex.getMessage().contains("Deluxe"));
        assertTrue(ex.getMessage().contains("3"));
    }

    @Test
    void processRoomSelections_shouldThrow_whenTotalExceedsMaxRooms() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(checkIn, checkOut))
                .thenReturn(List.of(
                        availability(1L, "Deluxe", new BigDecimal("200"), 10, 10),
                        availability(2L, "Suite", new BigDecimal("400"), 10, 10)
                ));

        SystemConfig config = new SystemConfig();
        config.setMaximumRoomPerBook(3);
        when(systemConfigService.getConfig()).thenReturn(config);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.processRoomSelections(draft, List.of(1L, 2L), List.of(2, 2)));
        assertTrue(ex.getMessage().contains("4"));
        assertTrue(ex.getMessage().contains("3"));
    }

    @Test
    void processRoomSelections_shouldHandleMultipleRoomTypes() {
        BookingDraft draft = draftWithDates();
        when(roomAvailabilityService.getAvailability(checkIn, checkOut))
                .thenReturn(List.of(
                        availability(1L, "Deluxe", new BigDecimal("150"), 10, 10),
                        availability(2L, "Suite", new BigDecimal("300"), 5, 5)
                ));

        SystemConfig config = new SystemConfig();
        config.setMaximumRoomPerBook(10);
        config.setDefaultDepositPercentage(new BigDecimal("30"));
        when(systemConfigService.getConfig()).thenReturn(config);

        service.processRoomSelections(draft, List.of(1L, 2L), List.of(2, 1));

        BookingDraft.BookingRooms rooms = draft.getRooms();
        assertEquals(2, rooms.selections().size());
        // totalPrice = (150 * 2 * 3) + (300 * 1 * 3) = 900 + 900 = 1800
        assertEquals(new BigDecimal("1800"), rooms.totalPrice());
        // deposit = 1800 * 30 / 100 = 540
        assertEquals(new BigDecimal("540"), rooms.depositAmount());
    }

    // --- initiateBookingOtp ---

    @Test
    void initiateBookingOtp_shouldReturnNull_whenCooldownActive() {
        BookingDraft draft = draftWithDates();
        draft.setGuest(new BookingDraft.BookingGuest("John", "john@test.com", null, null, null));

        when(otpService.createOtp("john@test.com", OtpType.BOOKING_CONFIRM)).thenReturn(null);

        String result = service.initiateBookingOtp(draft);

        assertNull(result);
        verify(emailService, never()).send(any(), any(), any());
        verify(emailService, never()).maskEmail(any());
    }

    @Test
    void initiateBookingOtp_shouldSendEmailAndReturnMasked_whenOtpCreated() {
        BookingDraft draft = draftWithDates();
        draft.setGuest(new BookingDraft.BookingGuest("John", "john@test.com", null, null, null));

        when(otpService.createOtp("john@test.com", OtpType.BOOKING_CONFIRM)).thenReturn("654321");
        when(emailService.maskEmail("john@test.com")).thenReturn("joh...");

        String result = service.initiateBookingOtp(draft);

        assertEquals("joh...", result);
        verify(emailService).send(eq("john@test.com"), eq("Your Booking OTP Code"), contains("654321"));
        verify(emailService).maskEmail("john@test.com");
    }

    // --- verifyBookingOtp ---

    @Test
    void verifyBookingOtp_shouldReturnNull_whenNotVerified() {
        when(otpService.verifyOtp("john@test.com", "wrong", OtpType.BOOKING_CONFIRM)).thenReturn(false);

        String result = service.verifyBookingOtp("john@test.com", "wrong");

        assertNull(result);
        verify(otpService, never()).issueToken(any(), any());
    }

    @Test
    void verifyBookingOtp_shouldReturnToken_whenVerified() {
        when(otpService.verifyOtp("john@test.com", "123456", OtpType.BOOKING_CONFIRM)).thenReturn(true);
        when(otpService.issueToken("john@test.com", OtpType.BOOKING_CONFIRM)).thenReturn("token-abc");

        String result = service.verifyBookingOtp("john@test.com", "123456");

        assertEquals("token-abc", result);
        verify(otpService).issueToken("john@test.com", OtpType.BOOKING_CONFIRM);
    }

    // --- createPendingReservation ---

    @Test
    void createPendingReservation_shouldDelegateAndReturn() {
        BookingDraft draft = draftWithDates();
        Reservation expected = new Reservation();
        String idempotencyKey = UUID.randomUUID().toString();

        when(reservationService.createOnlineBookingPending(draft, idempotencyKey)).thenReturn(expected);

        Reservation result = service.createPendingReservation(draft, idempotencyKey);

        assertSame(expected, result);
        verify(reservationService).createOnlineBookingPending(draft, idempotencyKey);
    }
}
