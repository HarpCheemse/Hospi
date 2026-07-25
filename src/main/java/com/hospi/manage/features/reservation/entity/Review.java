package com.hospi.manage.features.reservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing a guest review (rating) for a completed reservation.
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Set the creation timestamp before persisting.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
