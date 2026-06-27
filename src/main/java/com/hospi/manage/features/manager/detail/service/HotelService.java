package com.hospi.manage.features.manager.detail.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.utils.ImageUtils;
import com.hospi.manage.features.manager.detail.dto.HotelForm;
import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.entity.HotelPicture;
import com.hospi.manage.features.manager.detail.repository.HotelPictureRepository;
import com.hospi.manage.features.manager.detail.repository.HotelRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;
    private final HotelPictureRepository hotelPictureRepository;

    HotelService(HotelRepository hotelRepository, HotelPictureRepository hotelPictureRepository) {
        this.hotelRepository = hotelRepository;
        this.hotelPictureRepository = hotelPictureRepository;
    }

    /** Retrieve the single hotel entity. */
    public Hotel find() {
        return hotelRepository.findById(HotelConstants.HOTEL_ID).orElseThrow(
                () -> new ResourceNotFoundException("Hotel")
        );
    }

    /** Return the hotel edit form populated with current values. */
    public HotelForm getForm() {
        Hotel hotel = find();
        HotelForm form = new HotelForm();

        form.setName(hotel.getName());
        form.setDescription(hotel.getDescription());
        form.setAddress(hotel.getAddress());
        form.setPhone(hotel.getPhone());
        form.setEmail(hotel.getEmail());
        form.setCheckInTime(hotel.getCheckInTime());
        form.setCheckOutTime(hotel.getCheckOutTime());
        form.setFeatures(hotel.getFeatures());
        form.setStatus(hotel.getStatus());

        return form;
    }

    /**
     * Update hotel details and manage images (cover, gallery additions, removals).
     *
     * @param form            hotel metadata fields
     * @param coverImage      new cover image (replaces existing cover, sortOrder=0)
     * @param newImages       new gallery images to append
     * @param removeImageIds  IDs of existing gallery images to delete
     */
    @Transactional
    public void update(HotelForm form, MultipartFile coverImage, MultipartFile[] newImages, List<Long> removeImageIds)
            throws IOException {
        Hotel hotel = find();

        hotel.setName(form.getName());
        hotel.setDescription(form.getDescription());
        hotel.setAddress(form.getAddress());
        hotel.setPhone(form.getPhone());
        hotel.setEmail(form.getEmail());
        hotel.setCheckInTime(form.getCheckInTime());
        hotel.setCheckOutTime(form.getCheckOutTime());
        hotel.setFeatures(form.getFeatures());
        hotel.setStatus(form.getStatus());

        // Remove images by ID
        if (removeImageIds != null && !removeImageIds.isEmpty()) {
            List<HotelPicture> toRemove = hotelPictureRepository.findAllById(removeImageIds);
            hotel.getPictures().removeAll(toRemove);
            hotelPictureRepository.deleteAll(toRemove);
        }

        // Replace cover image
        if (coverImage != null && !coverImage.isEmpty()) {
            byte[] compressed = ImageUtils.compressWebP(coverImage, 720, 0.75f);
            HotelPicture cover = hotel.getPictures().stream()
                    .filter(p -> p.getSortOrder() != null && p.getSortOrder() == 0)
                    .findFirst()
                    .orElseGet(() -> {
                        HotelPicture p = new HotelPicture();
                        p.setHotel(hotel);
                        p.setSortOrder(0);
                        hotel.getPictures().add(p);
                        return p;
                    });
            cover.setImageData(compressed);
        }

        // Append new gallery images
        if (newImages != null) {
            int nextSort = hotel.getPictures().stream()
                    .mapToInt(p -> p.getSortOrder() != null ? p.getSortOrder() : 0)
                    .max()
                    .orElse(0) + 1;

            for (MultipartFile file : newImages) {
                if (!file.isEmpty()) {
                    byte[] compressed = ImageUtils.compressWebP(file, 720, 0.75f);
                    HotelPicture pic = new HotelPicture();
                    pic.setHotel(hotel);
                    pic.setImageData(compressed);
                    pic.setSortOrder(nextSort++);
                    hotel.getPictures().add(pic);
                }
            }
        }

        hotelRepository.save(hotel);
    }
}
