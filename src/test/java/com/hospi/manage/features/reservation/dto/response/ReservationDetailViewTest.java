package com.hospi.manage.features.reservation.dto.response;

import com.hospi.manage.features.reservation.entity.ReservationDetail;
import com.hospi.manage.features.room.entity.RoomType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ReservationDetailViewTest {

    @Test
    void shouldMapFromEntity() {
        var type = new RoomType();
        type.setId(5L);
        type.setName("Deluxe King");

        var detail = new ReservationDetail();
        detail.setRoomType(type);
        detail.setRoomCount(2);
        detail.setBasePrice(BigDecimal.valueOf(150));
        detail.setTotalPrice(BigDecimal.valueOf(300));

        var view = ReservationDetailView.from(detail);

        assertEquals(5L, view.roomTypeId());
        assertEquals("Deluxe King", view.roomTypeName());
        assertEquals(2, view.roomCount());
        assertEquals(BigDecimal.valueOf(150), view.basePrice());
        assertEquals(BigDecimal.valueOf(300), view.totalPrice());
    }

    @Test
    void shouldPreserveNullPrice() {
        var type = new RoomType();
        type.setId(1L);
        type.setName("Standard");

        var detail = new ReservationDetail();
        detail.setRoomType(type);
        detail.setRoomCount(1);
        detail.setBasePrice(null);
        detail.setTotalPrice(null);

        var view = ReservationDetailView.from(detail);

        assertNull(view.basePrice());
        assertNull(view.totalPrice());
    }
}
