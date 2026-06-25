package com.hospi.manage.features.room.entity;

import com.hospi.manage.features.room.enums.BedType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JPA entity representing a bookable room category (e.g. Deluxe, Suite) with pricing and amenity details.
 */
@Entity
@Table(name = "room_types")
@Getter
@Setter
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "max_occupancy")
    private Integer maxOccupancy;

    private String description;

    @Column(columnDefinition = "TEXT")
    private String features;

    @Enumerated(EnumType.STRING)
    @Column(name = "bed_type")
    private BedType bedType;

    private Integer area;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    private RoomCategory category;

    @Enumerated(EnumType.STRING)
    private RoomTier tier;

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

    /**
     * Set the creation and update timestamps before persisting.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }


    /**
     * Update the modification timestamp before updating.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Return the picture with sort order 1 (the cover image), or {@code null} if none exists.
     */
    public RoomTypePicture getCoverPicture() {
        return pictures.stream().filter(p -> Integer.valueOf(1).equals(p.getSortOrder())).findFirst().orElse(null);
    }

    /**
     * Return the ID of the cover picture, or {@code null} if no cover is set.
     */
    public Long getCoverPictureId() {
        return Optional.ofNullable(getCoverPicture()).map(RoomTypePicture::getId).orElse(null);
    }

    /**
     * Return the list of picture IDs excluding the cover image.
     */
    public List<Long> getPicturesExcludeCover() {
        return pictures.stream().filter(p -> !Integer.valueOf(1).equals(p.getSortOrder())).map(RoomTypePicture::getId).toList();
    }
}
