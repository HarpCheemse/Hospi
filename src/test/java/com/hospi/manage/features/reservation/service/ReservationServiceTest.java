package com.hospi.manage.features.reservation.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.reservation.dto.request.OfflineBookingForm;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.reservation.enums.BookingSource;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.ReservationRepository;
import com.hospi.manage.features.room.dto.response.RoomSelection;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private com.hospi.manage.features.payment.repository.PaymentRepository paymentRepository;

    @Mock
    private RoomAvailabilityService roomAvailabilityService;

    @Mock
    private com.hospi.manage.features.notification.service.NotificationService notificationService;

    @Mock
    private StayingGuestService stayingGuestService;

    @Mock
    private RoomAssignmentService roomAssignmentService;

    @InjectMocks
    private ReservationService reservationService;

    private final LocalDate today = LocalDate.now();
    private final LocalDate futureCheckIn = LocalDate.now().plusDays(5);
    private final LocalDate futureCheckOut = LocalDate.now().plusDays(10);

    // --- createReservation ---

    @Test
    void createReservation_shouldCreate_whenValidForm() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setBasePrice(new BigDecimal("200.00"));

        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", "john@example.com", "+1234567890",
                LocalDate.of(1990, 1, 1), "US",
                List.of(new RoomSelection(1L, 2))
        );

        when(roomAvailabilityService.canFulfil(any(), any(), any(), any())).thenReturn(true);
        when(roomTypeRepository.findAllById(any())).thenReturn(List.of(roomType));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation(form);

        assertEquals(ReservationStatus.PENDING, result.getStatus());
        assertEquals(BookingSource.OFFLINE, result.getSource());
        assertEquals("John Doe", result.getGuestName());
        assertEquals("john@example.com", result.getGuestEmail());
        assertEquals("+1234567890", result.getGuestPhone());
        assertEquals(LocalDate.of(1990, 1, 1), result.getGuestDateOfBirth());
        assertEquals("US", result.getGuestNationality());
        assertEquals(futureCheckIn, result.getCheckInAt());
        assertEquals(futureCheckOut, result.getCheckOutAt());
        assertEquals(1, result.getDetails().size());
        assertEquals(new BigDecimal("400.00"), result.getTotalPrice());
        verify(reservationRepository).save(result);
    }

    @Test
    void createReservation_shouldSkip_whenRoomCountNull() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setBasePrice(new BigDecimal("200.00"));

        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", null, "+1234567890",
                LocalDate.of(1990, 1, 1), null,
                List.of(new RoomSelection(1L, null), new RoomSelection(1L, 2))
        );

        when(roomAvailabilityService.canFulfil(any(), any(), any(), any())).thenReturn(true);
        when(roomTypeRepository.findAllById(any())).thenReturn(List.of(roomType));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation(form);

        assertEquals(1, result.getDetails().size());
        assertEquals(new BigDecimal("400.00"), result.getTotalPrice());
    }

    @Test
    void createReservation_shouldSkip_whenRoomCountZero() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setBasePrice(new BigDecimal("200.00"));

        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", null, "+1234567890",
                LocalDate.of(1990, 1, 1), null,
                List.of(new RoomSelection(1L, 0))
        );

        when(roomAvailabilityService.canFulfil(any(), any(), any(), any())).thenReturn(true);
        when(roomTypeRepository.findAllById(any())).thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation(form);

        assertTrue(result.getDetails().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
        verify(roomTypeRepository).findAllById(any());
    }

    @Test
    void createReservation_shouldThrow_whenRoomTypeNotFound() {
        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", null, "+1234567890",
                LocalDate.of(1990, 1, 1), null,
                List.of(new RoomSelection(999L, 2))
        );

        when(roomAvailabilityService.canFulfil(any(), any(), any(), any())).thenReturn(true);
        when(roomTypeRepository.findAllById(any())).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.createReservation(form));
    }

    @Test
    void createReservation_shouldSetGuestFields() {
        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "Jane Smith", "jane@test.com", "+9876543210",
                LocalDate.of(1985, 5, 15), "UK",
                List.of()
        );

        when(roomAvailabilityService.canFulfil(any(), any(), any(), any())).thenReturn(true);
        when(roomTypeRepository.findAllById(any())).thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation(form);

        assertEquals("Jane Smith", result.getGuestName());
        assertEquals("jane@test.com", result.getGuestEmail());
        assertEquals("+9876543210", result.getGuestPhone());
        assertEquals(LocalDate.of(1985, 5, 15), result.getGuestDateOfBirth());
        assertEquals("UK", result.getGuestNationality());
    }

    @Test
    void createReservation_shouldCalculateTotalPrice() {
        RoomType rt1 = new RoomType();
        rt1.setId(1L);
        rt1.setBasePrice(new BigDecimal("150.00"));

        RoomType rt2 = new RoomType();
        rt2.setId(2L);
        rt2.setBasePrice(new BigDecimal("300.00"));

        OfflineBookingForm form = new OfflineBookingForm(
                futureCheckIn, futureCheckOut,
                "John Doe", null, "+1234567890",
                LocalDate.of(1990, 1, 1), null,
                List.of(new RoomSelection(1L, 2), new RoomSelection(2L, 1))
        );

        when(roomAvailabilityService.canFulfil(any(), any(), any(), any())).thenReturn(true);
        when(roomTypeRepository.findAllById(any())).thenReturn(List.of(rt1, rt2));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation(form);

        assertEquals(2, result.getDetails().size());
        assertEquals(new BigDecimal("600.00"), result.getTotalPrice());
    }

    // --- checkIn ---

    private void mockCheckInPreconditions() {
        when(stayingGuestService.getGuestCount(1L)).thenReturn(1);
        when(roomAssignmentService.getAssignedRoomCount(1L)).thenReturn(2);
    }

    private ReservationDetail aDetail(int roomCount) {
        ReservationDetail d = new ReservationDetail();
        d.setRoomCount(roomCount);
        return d;
    }

    @Test
    void checkIn_shouldSucceed_whenConfirmedAndOffline() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setSource(BookingSource.OFFLINE);
        reservation.setCheckInAt(LocalDate.now());
        reservation.setCheckOutAt(LocalDate.now().plusDays(1));
        reservation.setDetails(List.of(aDetail(2)));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        mockCheckInPreconditions();

        Reservation result = reservationService.checkIn(1L, LocalDate.now(), null, "receptionist");

        assertEquals(ReservationStatus.CHECKED_IN, result.getStatus());
        assertNotNull(result.getCheckedInAt());
        assertEquals("receptionist", result.getCheckedInBy());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void checkIn_shouldThrow_whenNotConfirmed() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> reservationService.checkIn(1L, LocalDate.now(), null, "receptionist"));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void checkIn_shouldSucceed_whenOnlineWithValidCode() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setSource(BookingSource.ONLINE);
        reservation.setConfirmationCode("HSP-ABC123");
        reservation.setCheckInAt(LocalDate.now());
        reservation.setCheckOutAt(LocalDate.now().plusDays(1));
        reservation.setDetails(List.of(aDetail(2)));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        mockCheckInPreconditions();

        Reservation result = reservationService.checkIn(1L, LocalDate.now(), "HSP-ABC123", "receptionist");

        assertEquals(ReservationStatus.CHECKED_IN, result.getStatus());
    }

    @Test
    void checkIn_shouldThrow_whenOnlineWithMissingCode() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setSource(BookingSource.ONLINE);
        reservation.setCheckInAt(LocalDate.now());
        reservation.setCheckOutAt(LocalDate.now().plusDays(1));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> reservationService.checkIn(1L, LocalDate.now(), null, "receptionist"));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void checkIn_shouldThrow_whenOnlineWithWrongCode() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setSource(BookingSource.ONLINE);
        reservation.setConfirmationCode("HSP-ABC123");
        reservation.setCheckInAt(LocalDate.now());
        reservation.setCheckOutAt(LocalDate.now().plusDays(1));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> reservationService.checkIn(1L, LocalDate.now(), "WRONG-CODE", "receptionist"));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void checkIn_shouldThrow_whenReservationNotFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.checkIn(999L, LocalDate.now(), null, "receptionist"));
    }

    @Test
    void checkIn_shouldThrow_whenNoGuests() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setSource(BookingSource.OFFLINE);
        reservation.setCheckInAt(LocalDate.now());
        reservation.setCheckOutAt(LocalDate.now().plusDays(1));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(stayingGuestService.getGuestCount(1L)).thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> reservationService.checkIn(1L, LocalDate.now(), null, "receptionist"));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void checkIn_shouldThrow_whenRoomsNotAssigned() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setSource(BookingSource.OFFLINE);
        reservation.setCheckInAt(LocalDate.now());
        reservation.setCheckOutAt(LocalDate.now().plusDays(1));
        reservation.setDetails(List.of(aDetail(2)));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(stayingGuestService.getGuestCount(1L)).thenReturn(1);
        when(roomAssignmentService.getAssignedRoomCount(1L)).thenReturn(1);

        assertThrows(IllegalStateException.class,
                () -> reservationService.checkIn(1L, LocalDate.now(), null, "receptionist"));

        verify(reservationRepository, never()).save(any());
    }

    // --- extendStay ---

    @Test
    void extendStay_shouldExtend_whenValid() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setBasePrice(new BigDecimal("200.00"));

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(2);
        detail.setBasePrice(new BigDecimal("200.00"));
        detail.setTotalPrice(new BigDecimal("400.00"));

        LocalDate originalCheckOut = LocalDate.now().plusDays(3);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setCheckOutAt(originalCheckOut);
        reservation.setTotalPrice(new BigDecimal("800.00"));
        reservation.setDetails(List.of(detail));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomAvailabilityService.canFulfil(any(), any(), any(), any())).thenReturn(true);
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.extendStay(1L, 2);

        assertEquals(LocalDate.now().plusDays(5), result.getCheckOutAt());
        assertEquals(new BigDecimal("1600.00"), result.getTotalPrice());
        verify(roomAvailabilityService).canFulfil(any(), eq(originalCheckOut),
                eq(LocalDate.now().plusDays(5)), eq(1L));
    }

    @Test
    void extendStay_shouldThrow_whenExtraDaysZero() {
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.extendStay(1L, 0));
    }

    @Test
    void extendStay_shouldThrow_whenExtraDaysNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.extendStay(1L, -1));
    }

    @Test
    void extendStay_shouldThrow_whenNotCheckedIn() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> reservationService.extendStay(1L, 2));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void extendStay_shouldThrow_whenRoomsNotAvailable() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setBasePrice(new BigDecimal("200.00"));

        ReservationDetail detail = new ReservationDetail();
        detail.setRoomType(roomType);
        detail.setRoomCount(1);
        detail.setBasePrice(new BigDecimal("200.00"));

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setCheckOutAt(LocalDate.now().plusDays(2));
        reservation.setTotalPrice(new BigDecimal("400.00"));
        reservation.setDetails(List.of(detail));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(roomAvailabilityService.canFulfil(any(), any(), any(), any())).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> reservationService.extendStay(1L, 3));
    }

    // --- findByStatus ---

    @Test
    void findByStatus_shouldDelegate() {
        when(reservationRepository.findByStatusOrderByCheckInAtDesc(ReservationStatus.CHECKED_IN))
                .thenReturn(List.of(new Reservation()));

        List<Reservation> result = reservationService.findByStatus(ReservationStatus.CHECKED_IN);

        assertEquals(1, result.size());
        verify(reservationRepository).findByStatusOrderByCheckInAtDesc(ReservationStatus.CHECKED_IN);
    }

    // --- findFiltered ---

    @Test
    void findFiltered_shouldDelegateWithoutDate() {
        List<ReservationStatus> statuses = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
        when(reservationRepository.findFiltered(eq(statuses), eq("%")))
                .thenReturn(List.of(new Reservation()));

        List<Reservation> result = reservationService.findFiltered(statuses, null, null);

        assertEquals(1, result.size());
        verify(reservationRepository).findFiltered(eq(statuses), eq("%"));
    }

    @Test
    void findFiltered_shouldDelegateWithDate() {
        List<ReservationStatus> statuses = List.of(ReservationStatus.CONFIRMED);
        LocalDate date = LocalDate.now();

        when(reservationRepository.findFilteredWithDate(eq(statuses), eq("%"), eq(date)))
                .thenReturn(List.of(new Reservation()));

        List<Reservation> result = reservationService.findFiltered(statuses, null, date);

        assertEquals(1, result.size());
        verify(reservationRepository).findFilteredWithDate(eq(statuses), eq("%"), eq(date));
    }

    @Test
    void findFiltered_shouldTrimAndLowerSearch() {
        List<ReservationStatus> statuses = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        reservationService.findFiltered(statuses, "  JOHN  ", null);

        verify(reservationRepository).findFiltered(eq(statuses), eq("%john%"));
    }

    @Test
    void findFiltered_shouldUseWildcard_whenSearchBlank() {
        List<ReservationStatus> statuses = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        reservationService.findFiltered(statuses, "   ", null);

        verify(reservationRepository).findFiltered(eq(statuses), eq("%"));
    }

    @Test
    void findFiltered_shouldUseWildcard_whenSearchNull() {
        List<ReservationStatus> statuses = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        reservationService.findFiltered(statuses, null, null);

        verify(reservationRepository).findFiltered(eq(statuses), eq("%"));
    }

    // --- findById ---

    @Test
    void findById_shouldReturn_whenFound() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.findById(999L));
    }

    // --- cancelReservation ---

    @Test
    void cancelReservation_shouldCancel_whenPending() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(1L);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void cancelReservation_shouldThrow_whenCheckedIn() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CHECKED_IN);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> reservationService.cancelReservation(1L));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_shouldCancel_whenConfirmed() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(1L);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    // --- confirmAndAddPayment ---

    @Test
    void confirmAndAddPayment_shouldConfirmAndCreatePayment() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setTotalPrice(BigDecimal.valueOf(200));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.confirmAndAddPayment(1L, BigDecimal.valueOf(100), "ONLINE_BOOKING", "ORDER-123");

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals("ORDER-123", reservation.getPaymentIdempotencyKey());
        verify(reservationRepository).save(reservation);
        verify(paymentRepository).save(any());
    }

    @Test
    void confirmAndAddPayment_shouldThrow_whenNotFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.confirmAndAddPayment(999L, BigDecimal.valueOf(100), "ONLINE_BOOKING", "ORDER-123"));
    }
}
