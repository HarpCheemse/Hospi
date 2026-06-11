package com.hospi.manage.features.room.service;

import com.hospi.manage.features.room.entity.RoomTypePicture;
import com.hospi.manage.features.room.repository.RoomTypePictureRepository;
import org.springframework.stereotype.Service;

@Service
public class RoomTypePictureService {
    private final RoomTypePictureRepository roomTypePictureRepository;

    public RoomTypePictureService(RoomTypePictureRepository roomTypePictureRepository) {
        this.roomTypePictureRepository = roomTypePictureRepository;
    }

    public RoomTypePicture findById(Long id) {
        return roomTypePictureRepository
                .findById(id)
                .orElseThrow();
    }
}
