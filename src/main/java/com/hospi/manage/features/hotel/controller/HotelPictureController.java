package com.hospi.manage.features.hotel.controller;

import com.hospi.manage.features.hotel.entity.HotelPicture;
import com.hospi.manage.features.hotel.service.HotelPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** Controller for serving hotel banner images stored in the database. */
@Controller
@RequestMapping("/hotel-picture")
@RequiredArgsConstructor
public class HotelPictureController {

    private final HotelPictureService hotelPictureService;

    /**
     * Serve a hotel picture by ID as a WebP image response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable Long id
    ) {

        HotelPicture image =
                hotelPictureService.findById(id);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.valueOf("image/webp")
                )
                .body(image.getImageData());
    }
}
