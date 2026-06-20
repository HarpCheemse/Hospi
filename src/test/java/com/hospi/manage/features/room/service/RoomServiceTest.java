package com.hospi.manage.features.room.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.repository.HotelRepository;
import com.hospi.manage.features.reservation.entity.RoomAssignment;
import com.hospi.manage.features.reservation.enums.ReservationStatus;
import com.hospi.manage.features.room.dto.request.RoomCreateForm;
import com.hospi.manage.features.room.dto.request.RoomEditForm;
import com.hospi.manage.features.room.dto.response.FloorView;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.ConditionStatus;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import com.hospi.manage.features.reservation.repository.RoomAssignmentRepository;
import com.hospi.manage.features.reservation.repository.StayingGuestRepository;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
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

    @InjectMocks
    private RoomService roomService;

    @Test
    void findAll_shouldReturnActiveOnly() {
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
    void findById_shouldReturn_whenActive() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        Room result = roomService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldThrow_whenInactive() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(false);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.findById(1L));
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(roomRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.findById(1L));
    }

    @Test
    void updateRoom_shouldUpdate() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);
        room.setFloorNumber((short) 1);
        room.setRoomNumber("101");

        RoomType roomType = new RoomType();
        roomType.setId(2L);
        roomType.setActive(true);

        RoomEditForm form = new RoomEditForm("102", 2L, ConditionStatus.DIRTY);

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
    void updateRoom_shouldThrow_whenRoomTypeInactive() {
        Room room = new Room();
        room.setId(1L);
        room.setActive(true);

        RoomType roomType = new RoomType();
        roomType.setId(2L);
        roomType.setActive(false);

        RoomEditForm form = new RoomEditForm("102", 2L, ConditionStatus.CLEAN);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));
        when(roomTypeRepository.findById(2L))
                .thenReturn(Optional.of(roomType));

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.updateRoom(1L, form));
    }

    @Test
    void createRoom_shouldCreateRooms() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setActive(true);

        RoomCreateForm form = new RoomCreateForm((short) 1, 3, 1L);

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
    void createRoom_shouldIncrementFromHighest() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setActive(true);

        RoomCreateForm form = new RoomCreateForm((short) 1, 2, 1L);

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
    void createRoom_shouldThrow_whenRoomTypeInactive() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setActive(false);

        RoomCreateForm form = new RoomCreateForm((short) 1, 1, 1L);

        when(roomRepository.findHighestRoomNumberByFloor((short) 1))
                .thenReturn(null);
        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.createRoom(form));
    }

    @Test
    void getFloorViews_shouldReturnActiveOnly() {
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
    void generateDefaultRoomLayout_shouldSucceed() {
        when(roomRepository.count()).thenReturn(0L);

        Hotel hotel = new Hotel();
        hotel.setFloorCount((short) 2);
        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        RoomType rt1 = new RoomType();
        rt1.setId(1L);
        rt1.setActive(true);

        RoomType rt2 = new RoomType();
        rt2.setId(2L);
        rt2.setActive(true);

        when(roomTypeRepository.findByActiveTrue())
                .thenReturn(List.of(rt1, rt2));

        boolean result = roomService.generateDefaultRoomLayout();

        assertTrue(result);
        verify(roomRepository).saveAll(any());
    }

    @Test
    void generateDefaultRoomLayout_shouldReturnFalse_whenRoomsExist() {
        when(roomRepository.count()).thenReturn(5L);

        boolean result = roomService.generateDefaultRoomLayout();

        assertFalse(result);
        verify(roomRepository, never()).saveAll(any());
    }

    @Test
    void delete_shouldSoftDelete() {
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
    void delete_shouldThrow_whenActiveReservationsExist() {
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
}
