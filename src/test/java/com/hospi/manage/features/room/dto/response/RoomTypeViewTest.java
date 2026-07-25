package com.hospi.manage.features.room.dto.response;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RoomTypeViewTest {

    @Test
    void allPictureIds_shouldReturnCoverFirstThenGallery() {
        RoomTypeView view = new RoomTypeView(
            1L, "Test", null, null, null, 0, 0, BigDecimal.ZERO, 0,
            100L, "desc", "wifi", List.of(200L, 300L),
            null, null, null
        );
        assertEquals(List.of(100L, 200L, 300L), view.allPictureIds());
    }

    @Test
    void allPictureIds_shouldHandleNullCover() {
        RoomTypeView view = new RoomTypeView(
            1L, "Test", null, null, null, 0, 0, BigDecimal.ZERO, 0,
            null, "desc", "wifi", List.of(200L, 300L),
            null, null, null
        );
        assertEquals(List.of(200L, 300L), view.allPictureIds());
    }

    @Test
    void allPictureIds_shouldHandleEmptyGallery() {
        RoomTypeView view = new RoomTypeView(
            1L, "Test", null, null, null, 0, 0, BigDecimal.ZERO, 0,
            100L, "desc", "wifi", List.of(),
            null, null, null
        );
        assertEquals(List.of(100L), view.allPictureIds());
    }

    @Test
    void allPictureIds_shouldHandleNullCoverAndEmptyGallery() {
        RoomTypeView view = new RoomTypeView(
            1L, "Test", null, null, null, 0, 0, BigDecimal.ZERO, 0,
            null, "desc", "wifi", List.of(),
            null, null, null
        );
        assertTrue(view.allPictureIds().isEmpty());
    }
}
