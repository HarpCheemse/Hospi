package com.hospi.manage.features.reservation.entity;

import com.hospi.manage.features.room.entity.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity representing a line item on a reservation, linking a room type with a quantity and price.
 */
@Entity
@Table(name = "reservation_details")
@Getter
@Setter
public class ReservationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "room_count", nullable = false)
    private Integer roomCount;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "total_price")
    private BigDecimal totalPrice;
}
