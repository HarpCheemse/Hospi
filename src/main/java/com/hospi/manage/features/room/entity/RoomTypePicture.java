package com.hospi.manage.features.room.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing an image (cover or gallery) associated with a room type.
 */
@Entity
@Table(name = "room_type_pictures")
@Getter
@Setter
public class RoomTypePicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "BYTEA", name = "image_data")
    private byte[] imageData;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    /**
     * Set the creation timestamp before persisting.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Return {@code true} if this picture is the cover image (sort order is 1).
     */
    public boolean isCover() {
        return Integer.valueOf(1).equals(this.sortOrder);
    }

    /**
     * Return {@code true} if this picture is not the cover image (gallery image).
     */
    public boolean isAdditional() {
        return !isCover();
    }
}