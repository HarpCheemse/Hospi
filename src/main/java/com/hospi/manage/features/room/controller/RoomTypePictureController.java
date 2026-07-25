package com.hospi.manage.features.room.controller;

import com.hospi.manage.features.room.entity.RoomTypePicture;
import com.hospi.manage.features.room.service.RoomTypePictureService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for serving room type images (cover and gallery) stored in the database.
 */
@Controller
@RequestMapping("/room-type-picture")
public class RoomTypePictureController {

    private final RoomTypePictureService roomTypePictureService;

    /**
     * Construct the controller with required service.
     */
    public RoomTypePictureController(
            RoomTypePictureService roomTypePictureService
    ) {
        this.roomTypePictureService = roomTypePictureService;
    }

    /**
     * Serve a room type picture by ID as a WebP image response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long id
    ) {

        RoomTypePicture image =
                roomTypePictureService.findById(id);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.valueOf("image/webp")
                )
                .body(image.getImageData());
    }
}
