package com.hospi.manage.features.manager.room.repository;

import com.hospi.manage.features.manager.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    public List<Room> findByFloorNumberOrderByRoomNumber(int floor);
}
