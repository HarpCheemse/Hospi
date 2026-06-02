package com.hospi.manage.features.manager.room.repository;

import com.hospi.manage.features.manager.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    public List<Room> findByFloorNumberOrderByRoomNumber(int floor);

    @Query("""
                select max(cast(r.roomNumber as integer))
                from Room r
                where r.floorNumber = :floor
            """)
    Integer findHighestRoomNumberByFloor(short floor);

    public Optional<Room> findByRoomNumber(String roomNumber);

    public boolean existsByRoomNumber(String roomNumber);
}
