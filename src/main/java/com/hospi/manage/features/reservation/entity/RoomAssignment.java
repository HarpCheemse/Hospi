package com.hospi.manage.features.reservation.entity;

import com.hospi.manage.features.room.entity.Room;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing the assignment of a specific room to a reservation.
 */
@Entity
@Table(name = "room_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_id", "room_id"}))
@Getter
@Setter
public class RoomAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    /**
     * Set the assignment timestamp before persisting.
     */
    @PrePersist
    public void prePersist() {
        assignedAt = LocalDateTime.now();
    }
}
