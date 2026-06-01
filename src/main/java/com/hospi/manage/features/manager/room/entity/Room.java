package com.hospi.manage.features.manager.room.entity;

import com.hospi.manage.features.manager.room.enums.ConditionStatus;
import com.hospi.manage.features.manager.room.enums.OccupancyStatus;
import com.hospi.manage.features.manager.roomtype.entity.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @Column(name = "room_number", length = 10)
    private String roomNumber;

    @Column(name = "floor_number")
    private Short floorNumber;

    @Column(name = "is_active")
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupancy_status")
    private OccupancyStatus occupancyStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status")
    private ConditionStatus conditionStatus;
}
