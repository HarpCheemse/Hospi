package com.hospi.manage.features.room.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.account.enums.Role;
import com.hospi.manage.features.hotel.repository.HotelRepository;
import com.hospi.manage.features.notification.service.NotificationService;
import com.hospi.manage.features.reservation.entity.Reservation;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.entity.StayingGuest;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.reservation.repository.RoomAssignmentRepository;
import com.hospi.manage.features.room.dto.response.RoomOccupancyView;
import com.hospi.manage.features.reservation.repository.StayingGuestRepository;
import com.hospi.manage.features.room.dto.request.RoomCreateForm;
import com.hospi.manage.features.room.dto.request.RoomEditForm;
import com.hospi.manage.features.room.dto.response.FloorView;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomAssignmentRepository roomAssignmentRepository;

    @Mock
    private StayingGuestRepository stayingGuestRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RoomService roomService;

    @Test
    void shouldReturnActiveOnly_whenFindAll() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        when(roomRepository.findByActiveTrueOrderByFloorNumberAscRoomNumberAsc())
                .thenReturn(List.of(room));

        List<Room> result = roomService.findAll();

        assertEquals(1, result.size());
        verify(roomRepository).findByActiveTrueOrderByFloorNumberAscRoomNumberAsc();
    }

    @Test
    void shouldReturn_whenActiveAndFound() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        Room result = roomService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrow_whenInactive() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(false);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.findById(1L));
    }

    @Test
    void shouldThrow_whenNotFound() {
        when(roomRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.findById(1L));
    }

    @Test
    void shouldUpdate_whenValidForm() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setFloorNumber((short) 1);
        room.setRoomNumber("101");

        RoomType roomType = new RoomType();
        roomType.setId(2L);
        roomType.setActive(true);

        RoomEditForm form = new RoomEditForm("102",
                2L,
                ConditionStatus.DIRTY);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));
        when(roomTypeRepository.findById(2L))
                .thenReturn(Optional.of(roomType));

        roomService.updateRoom(1L, form);

        assertEquals("102", room.getRoomNumber());
        assertEquals(roomType, room.getRoomType());
        assertEquals(ConditionStatus.DIRTY, room.getConditionStatus());
        verify(roomRepository).save(room);
    }

    @Test
    void shouldThrow_whenEditingWithInactiveRoomType() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        RoomType roomType = new RoomType();
        roomType.setId(2L);
        roomType.setActive(false);

        RoomEditForm form = new RoomEditForm("102",
                2L,
                ConditionStatus.CLEAN);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));
        when(roomTypeRepository.findById(2L))
                .thenReturn(Optional.of(roomType));

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.updateRoom(1L, form));
    }

    @Test
    void shouldCreate_whenNoExistingRoomsOnFloor() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setActive(true);

        RoomCreateForm form = new RoomCreateForm((short) 1,
                3,
                1L);

        when(roomRepository.findHighestRoomNumberByFloor((short) 1))
                .thenReturn(null);
        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        roomService.createRoom(form);

        verify(roomRepository).saveAll(argThat(rooms -> {
            List<Room> list = (List<Room>) rooms;
            return list.size() == 3
                    && list.get(0).getRoomNumber().equals("101")
                    && list.get(1).getRoomNumber().equals("102")
                    && list.get(2).getRoomNumber().equals("103")
                    && list.get(0).getFloorNumber() == 1;
        }));
    }

    @Test
    void shouldIncrementRoomNumbers_whenExistingRoomsOnFloor() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setActive(true);

        RoomCreateForm form = new RoomCreateForm((short) 1,
                2,
                1L);

        when(roomRepository.findHighestRoomNumberByFloor((short) 1))
                .thenReturn(105);
        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        roomService.createRoom(form);

        verify(roomRepository).saveAll(argThat(rooms -> {
            List<Room> list = (List<Room>) rooms;
            return list.size() == 2
                    && list.get(0).getRoomNumber().equals("106")
                    && list.get(1).getRoomNumber().equals("107");
        }));
    }

    @Test
    void shouldThrow_whenCreatingWithInactiveRoomType() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setActive(false);

        RoomCreateForm form = new RoomCreateForm((short) 1,
                1,
                1L);

        when(roomRepository.findHighestRoomNumberByFloor((short) 1))
                .thenReturn(null);
        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.createRoom(form));
    }

    @Test
    void shouldReturnFloorViews_whenActiveRoomsExist() {
        Room room1 = new Room();
        room1.setId(1L);
        room1.setActive(true);
        room1.setFloorNumber((short) 1);
        room1.setRoomNumber("101");
        room1.setOccupancyStatus(OccupancyStatus.VACANT);
        room1.setConditionStatus(ConditionStatus.CLEAN);

        Room room2 = new Room();
        room2.setId(2L);
        room2.setActive(true);
        room2.setFloorNumber((short) 1);
        room2.setRoomNumber("102");
        room2.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        room2.setConditionStatus(ConditionStatus.DIRTY);

        when(roomRepository.findByActiveTrueOrderByFloorNumberAscRoomNumberAsc())
                .thenReturn(List.of(room1, room2));

        List<FloorView> result = roomService.getFloorViews();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).number());
        assertEquals(2, result.get(0).roomCount());
        assertEquals(2, result.get(0).rooms().size());
    }

    @Test
    void shouldSoftDelete_whenNoActiveReservations() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setRoomNumber("101");

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));
        when(roomAssignmentRepository.findByRoomIdAndReservation_StatusIn(1L,
                List.of(ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.CHECKED_IN)))
                .thenReturn(List.of());

        roomService.delete(1L);

        assertFalse(room.isActive());
        verify(roomRepository).save(room);
    }

    @Test
    void shouldThrow_whenActiveReservationsExist() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setRoomNumber("101");

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));
        when(roomAssignmentRepository.findByRoomIdAndReservation_StatusIn(1L,
                List.of(ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED,
                        ReservationStatus.CHECKED_IN)))
                .thenReturn(List.of(new RoomAssignment()));

        assertThrows(IllegalStateException.class,
                () -> roomService.delete(1L));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void shouldThrow_whenEditingRoomNotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        RoomEditForm form = new RoomEditForm("102", 2L, ConditionStatus.CLEAN);

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.updateRoom(1L, form));
    }

    @Test
    void shouldThrow_whenEditingRoomTypeNotFound() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        RoomEditForm form = new RoomEditForm("102", 99L, ConditionStatus.CLEAN);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.updateRoom(1L, form));
    }

    @Test
    void shouldNotifyReceptionist_whenConditionCleaned() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setFloorNumber((short) 1);
        room.setRoomNumber("101");
        room.setConditionStatus(ConditionStatus.DIRTY);

        RoomType roomType = new RoomType();
        roomType.setId(2L);
        roomType.setActive(true);

        RoomEditForm form = new RoomEditForm("101", 2L, ConditionStatus.CLEAN);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomTypeRepository.findById(2L)).thenReturn(Optional.of(roomType));

        roomService.updateRoom(1L, form);

        assertEquals(ConditionStatus.CLEAN, room.getConditionStatus());
        verify(roomRepository).save(room);
        verify(notificationService).notifyRole(eq(Role.RECEPTIONIST), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldReturnFilteredFloorViews_whenFloorSpecified() {
        Room room1 = new Room();
        room1.setId(1L);
        room1.setActive(true);
        room1.setFloorNumber((short) 1);
        room1.setRoomNumber("101");
        room1.setOccupancyStatus(OccupancyStatus.VACANT);
        room1.setConditionStatus(ConditionStatus.CLEAN);

        Room room2 = new Room();
        room2.setId(2L);
        room2.setActive(true);
        room2.setFloorNumber((short) 2);
        room2.setRoomNumber("201");
        room2.setOccupancyStatus(OccupancyStatus.VACANT);
        room2.setConditionStatus(ConditionStatus.CLEAN);

        when(roomRepository.findByActiveTrueOrderByFloorNumberAscRoomNumberAsc())
                .thenReturn(List.of(room1, room2));

        List<FloorView> result = roomService.getFloorViews(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).number());
    }

    @Test
    void shouldUpdateConditionStatus() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setConditionStatus(ConditionStatus.DIRTY);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        roomService.updateConditionStatus(1L, ConditionStatus.CLEAN);

        assertEquals(ConditionStatus.CLEAN, room.getConditionStatus());
        verify(roomRepository).save(room);
    }

    @Test
    void shouldReturnVacant_whenRoomNotOccupied() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setRoomNumber("101");
        room.setOccupancyStatus(OccupancyStatus.VACANT);
        room.setConditionStatus(ConditionStatus.CLEAN);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        RoomOccupancyView result = roomService.getRoomOccupancy(1L);

        assertEquals(1L, result.roomId());
        assertEquals(OccupancyStatus.VACANT, result.occupancyStatus());
    }

    @Test
    void shouldReturnVacant_whenOccupiedButNoAssignments() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setRoomNumber("101");
        room.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        room.setConditionStatus(ConditionStatus.CLEAN);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomAssignmentRepository.findByRoomIdAndReservation_StatusIn(1L, List.of(ReservationStatus.CHECKED_IN)))
                .thenReturn(List.of());

        RoomOccupancyView result = roomService.getRoomOccupancy(1L);

        assertEquals(OccupancyStatus.VACANT, result.occupancyStatus());
    }

    @Test
    void shouldReturnOccupied_whenRoomHasCheckedInGuests() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setRoomNumber("101");
        room.setOccupancyStatus(OccupancyStatus.OCCUPIED);
        room.setConditionStatus(ConditionStatus.CLEAN);

        RoomType roomType = new RoomType();
        roomType.setName("Deluxe");
        room.setRoomType(roomType);

        Reservation reservation = new Reservation();
        reservation.setId(10L);
        reservation.setGuestName("John Doe");
        reservation.setCheckInAt(LocalDate.now());
        reservation.setCheckOutAt(LocalDate.now().plusDays(2));

        RoomAssignment assignment = new RoomAssignment();
        assignment.setReservation(reservation);

        StayingGuest guest = new StayingGuest();
        guest.setGuestName("John Doe");
        guest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        guest.setNationality("US");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomAssignmentRepository.findByRoomIdAndReservation_StatusIn(1L, List.of(ReservationStatus.CHECKED_IN)))
                .thenReturn(List.of(assignment));
        when(stayingGuestRepository.findByReservationIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(guest));

        RoomOccupancyView result = roomService.getRoomOccupancy(1L);

        assertEquals(OccupancyStatus.OCCUPIED, result.occupancyStatus());
        assertEquals("John Doe", result.guestName());
        assertEquals(1, result.stayingGuests().size());
    }

    @Test
    void shouldThrow_whenCreatingRoomTypeNotFound() {
        RoomCreateForm form = new RoomCreateForm((short) 1, 1, 99L);

        when(roomRepository.findHighestRoomNumberByFloor((short) 1)).thenReturn(null);
        when(roomTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.createRoom(form));
    }

    @Test
    void shouldReturnEmptyViews_whenNoActiveRooms() {
        when(roomRepository.findByActiveTrueOrderByFloorNumberAscRoomNumberAsc())
                .thenReturn(List.of());

        List<FloorView> result = roomService.getFloorViews();

        assertTrue(result.isEmpty());
    }

    @Test
    void getActiveDirtyRooms_shouldReturnOnlyActiveDirtyRooms() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setConditionStatus(ConditionStatus.DIRTY);

        when(roomRepository.findByConditionStatusAndActiveTrueOrderByFloorNumberAscRoomNumberAsc(ConditionStatus.DIRTY))
                .thenReturn(List.of(room));

        List<Room> result = roomService.getActiveDirtyRooms();

        assertEquals(1, result.size());
        assertEquals(ConditionStatus.DIRTY, result.get(0).getConditionStatus());
        verify(roomRepository).findByConditionStatusAndActiveTrueOrderByFloorNumberAscRoomNumberAsc(ConditionStatus.DIRTY);
    }

    @Test
    void updateConditionStatus_shouldNotifyReceptionist_whenRoomBecomesClean() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setRoomNumber("101");
        room.setConditionStatus(ConditionStatus.DIRTY);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        roomService.updateConditionStatus(1L, ConditionStatus.CLEAN);

        assertEquals(ConditionStatus.CLEAN, room.getConditionStatus());
        verify(roomRepository).save(room);
        verify(notificationService).notifyRole(
                Role.RECEPTIONIST,
                "Room Ready",
                "Room 101 is now clean and available",
                "ROOM",
                "1"
        );
    }
}
