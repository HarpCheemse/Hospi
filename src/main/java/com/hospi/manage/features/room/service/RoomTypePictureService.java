package com.hospi.manage.features.room.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.service.ImageCompressionService;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.entity.RoomTypePicture;
import com.hospi.manage.features.room.repository.RoomTypePictureRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** Business logic for managing room type cover and gallery images. */
@Service
public class RoomTypePictureService {
    private final ImageCompressionService imageCompressionService;
    private final RoomTypePictureRepository roomTypePictureRepository;

    private static final int COVER_WIDTH = 1080;
    private static final int GALLERY_WIDTH = 720;
    private static final float IMAGE_QUALITY = 0.75f;

    private static final int COVER_ORDER = 1;
    private static final int GALLERY_ORDER = 2;

    public RoomTypePictureService(ImageCompressionService imageCompressionService,
                                  RoomTypePictureRepository roomTypePictureRepository) {
        this.imageCompressionService = imageCompressionService;
        this.roomTypePictureRepository = roomTypePictureRepository;
    }

    /** Find a room type picture by ID. */
    public RoomTypePicture findById(Long id) {
        return roomTypePictureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room Type Picture"));
    }

    /** Replace the cover image of a room type. */
    @Transactional
    public void replaceCover(RoomType roomType, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return;

        byte[] compressed = imageCompressionService.toWebp(file,
                COVER_WIDTH,
                IMAGE_QUALITY);

        roomType.getPictures().removeIf(RoomTypePicture::isCover);

        RoomTypePicture picture = new RoomTypePicture();
        picture.setRoomType(roomType);
        picture.setImageData(compressed);
        picture.setSortOrder(COVER_ORDER);

        roomType.getPictures().add(picture);
    }

    /** Add new gallery images to a room type. */
    @Transactional
    public void addNewImages(RoomType roomType, List<MultipartFile> files) throws IOException {

        if (files == null || files.isEmpty()) return;

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) continue;

            byte[] compressed = imageCompressionService.toWebp(file,
                    GALLERY_WIDTH,
                    IMAGE_QUALITY);

            RoomTypePicture picture = new RoomTypePicture();
            picture.setRoomType(roomType);
            picture.setImageData(compressed);
            picture.setSortOrder(GALLERY_ORDER);

            roomType.getPictures().add(picture);
        }
    }

    /** Remove specific gallery images from a room type. */
    @Transactional
    public void removeImages(RoomType roomType, List<Long> ids) {

        if (ids == null || ids.isEmpty()) return;

        roomType.getPictures().removeIf(p -> ids.contains(p.getId()));
    }
}
