package com.hospi.manage.features.hotel.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.hotel.entity.HotelPicture;
import com.hospi.manage.features.hotel.repository.HotelPictureRepository;
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
