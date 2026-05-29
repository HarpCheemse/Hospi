package com.hospi.manage.features.manager.roomtype.service;

import com.hospi.manage.common.utils.ImageUtils;
import com.hospi.manage.features.manager.roomtype.entity.RoomType;
import com.hospi.manage.features.manager.roomtype.entity.RoomTypePicture;
import com.hospi.manage.features.manager.roomtype.repository.RoomTypePictureRepository;
import com.hospi.manage.features.manager.roomtype.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
