package com.hospi.manage.features.room.repository;

import com.hospi.manage.features.room.dto.response.RoomInventory;
import com.hospi.manage.features.room.entity.Room;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.OccupancyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByActiveTrueOrderByFloorNumberAscRoomNumberAsc();

    Optional<Room> findByRoomNumberAndActiveTrue(String roomNumber);

    List<Room> findByRoomTypeIdAndOccupancyStatusAndActiveTrue(Long roomTypeId, OccupancyStatus occupancyStatus);

    boolean existsByRoomNumberAndActiveTrue(String roomNumber);

    long countByRoomTypeIdAndActiveTrue(Long roomTypeId);

    @Query("""
            SELECT MAX(CAST(r.roomNumber AS integer))
            FROM Room r
            WHERE r.floorNumber = :floor AND r.active = true
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
                .map(row -> {
                    RoomType rt = (RoomType) row[0];
                    return new RoomInventory(
                            rt.getId(),
                            rt.getName(),
                            ((Long) row[1]).intValue()
                    );
                })
                .toList();
    }
}