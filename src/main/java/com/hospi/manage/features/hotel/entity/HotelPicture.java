package com.hospi.manage.features.hotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing an image associated with a hotel.
 */
@Entity
@Table(name = "hotel_pictures")
@Getter
@Setter
public class HotelPicture {
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
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    /**
     * Set the creation timestamp before persisting.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
