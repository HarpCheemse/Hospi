package com.hospi.manage.features.manager.detail.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.manager.detail.entity.HotelPicture;
import com.hospi.manage.features.manager.detail.repository.HotelPictureRepository;
import org.springframework.stereotype.Service;

@Service
public class HotelPictureService {
    private final HotelPictureRepository hotelPictureRepository;

    HotelPictureService(HotelPictureRepository hotelPictureRepository) {
        this.hotelPictureRepository = hotelPictureRepository;
    }

    /** Find a hotel picture by ID. */
    public HotelPicture findById(Long id) {
        return hotelPictureRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Hotel picture"));
    }
}
