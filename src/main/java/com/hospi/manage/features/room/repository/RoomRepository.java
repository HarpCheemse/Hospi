package com.hospi.manage.features.room.repository;

import com.hospi.manage.features.room.dto.RoomInventory;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByFloorNumberOrderByRoomNumber(int floor);

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    @Query("""
            SELECT MAX(CAST(r.roomNumber AS integer))
            FROM Room r
            WHERE r.floorNumber = :floor
            """)
    Integer findHighestRoomNumberByFloor(short floor);

    @Query("""
            SELECT r.roomType, COUNT(r)
            FROM Room r
            WHERE r.active = true
            GROUP BY r.roomType
            """)
    List<Object[]> countActiveRoomsByType();

    default List<RoomInventory> getRoomInventory() {
        return countActiveRoomsByType()
                .stream()
                .map(row -> new RoomInventory(
                        (RoomType) row[0],
                        ((Long) row[1]).intValue()
                ))
                .toList();
    }
}