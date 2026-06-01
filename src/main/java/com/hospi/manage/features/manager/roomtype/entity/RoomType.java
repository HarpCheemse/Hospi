package com.hospi.manage.features.manager.roomtype.entity;

import com.hospi.manage.features.manager.room.entity.Room;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_types")
@Getter
@Setter
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    private String name;

    @Column(name = "max_occupancy")
    private Short maxOccupancy;

    private String description;

    @Column(columnDefinition = "TEXT")
    private String features;

    @Column(name = "bed_type")
    private String bedType;

    private Integer area;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomTypePicture> pictures = new ArrayList<>();

    @OneToMany(mappedBy = "roomType")
    private List<Room> rooms = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
