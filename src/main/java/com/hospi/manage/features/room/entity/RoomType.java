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
 * Represents a category of room (e.g. Deluxe, Suite).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code name} — display name of the room type</li>
 *   <li>{@code maxOccupancy} — maximum number of guests allowed</li>
 *   <li>{@code basePrice} — nightly rate before taxes/fees</li>
 *   <li>{@code bedType} — e.g. "King", "Twin", "Queen"</li>
 *   <li>{@code area} — room size in square meters</li>
 *   <li>{@code features} — free-text description of amenities</li>
 *   <li>{@code active} — whether this type is bookable</li>
 *   <li>{@code pictures} — associated images</li>
 *   <li>{@code rooms} — physical rooms of this type</li>
 * </ul>
 */
@Entity
@Table(name = "room_types")
@Getter
@Setter
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id")
    private Long hotelId;

    private String name;

    @Column(name = "max_occupancy")
    private Integer maxOccupancy;

    private String description;

    /**
     * Free-text list of amenities, stored as a comma-separated string.
     * Example: "WiFi, Minibar, Balcony"
     */
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

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }


    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public RoomTypePicture getCoverPicture() {
        return pictures.stream().filter(p -> Integer.valueOf(1).equals(p.getSortOrder())).findFirst().orElse(null);
    }

    public Long getCoverPictureId() {
        return Optional.ofNullable(getCoverPicture()).map(RoomTypePicture::getId).orElse(null);
    }

    public List<Long> getPicturesExcludeCover() {
        return pictures.stream().filter(p -> !Integer.valueOf(1).equals(p.getSortOrder())).map(RoomTypePicture::getId).toList();
    }
}
