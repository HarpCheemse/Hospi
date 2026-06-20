package com.hospi.manage.features.room.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.features.room.dto.request.RoomTypeCreateForm;
import com.hospi.manage.features.room.dto.request.RoomTypeEditForm;
import com.hospi.manage.features.room.dto.response.RoomTypeView;
import com.hospi.manage.features.room.entity.RoomType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import com.hospi.manage.features.room.repository.RoomRepository;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;
    private final RoomTypePictureService roomTypePictureService;
    private final RoomRepository roomRepository;

    private static final int CoverWidth = 1080;
    private static final int GALLERY_WIDTH = 720;
    private static final float IMAGE_QUALITY = 0.75f;

    public RoomTypeService(RoomTypeRepository roomTypeRepository,
                           RoomTypePictureService roomTypePictureService,
                           RoomRepository roomRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomTypePictureService = roomTypePictureService;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public RoomType createRoomType(RoomTypeCreateForm form) throws IOException {

        RoomType roomType = new RoomType();

        roomType.setName(generateRoomTypeName(form.category(),
                form.tier()));
        roomType.setMaxOccupancy(form.maxOccupancy());
        roomType.setDescription(form.description());
        roomType.setFeatures(form.features());
        roomType.setBedType(form.bedType());
        roomType.setArea(form.area());
        roomType.setBasePrice(form.basePrice());
        roomType.setActive(true);

        roomTypePictureService.replaceCover(roomType,
                form.coverImage());

        return roomTypeRepository.save(roomType);
    }

    private String generateRoomTypeName(RoomCategory category, RoomTier tier) {
        return tier.name() + " " + category.name();
    }

    public List<RoomType> findAll() {
        return roomTypeRepository.findByActiveTrue();
    }

    public RoomType findById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type"));
        if (!roomType.getActive()) {
            throw new ResourceNotFoundException("Room type");
        }
        return roomType;
    }

    @Transactional
    public void update(Long id, RoomTypeEditForm form) throws IOException {

        RoomType roomType = findById(id);

        roomType.setName(generateRoomTypeName(form.category(),
                form.tier()));
        roomType.setCategory(form.category());
        roomType.setTier(form.tier());
        roomType.setBedType(form.bedType());
        roomType.setMaxOccupancy(form.maxOccupancy());
        roomType.setArea(form.area());
        roomType.setBasePrice(form.basePrice());
        roomType.setDescription(form.description());
        roomType.setFeatures(form.features());

        roomTypePictureService.replaceCover(roomType,
                form.coverImage());
        roomTypePictureService.addNewImages(roomType,
                form.newImages());
        roomTypePictureService.removeImages(roomType,
                form.removeImageIds());

        roomTypeRepository.save(roomType);
    }

    @Transactional
    public void delete(Long id) {
        RoomType roomType = findById(id);
        long activeRoomCount = roomRepository.countByRoomTypeIdAndActiveTrue(id);
        if (activeRoomCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete room type with " + activeRoomCount + " active room(s) assigned"
            );
        }
        roomType.setActive(false);
        roomTypeRepository.save(roomType);
    }

    public List<RoomTypeView> findAllViews() {
        return roomTypeRepository.findAllWithRelations().stream().map(RoomTypeView::from).toList();
    }

    public RoomTypeView findViewById(Long id) {

        return roomTypeRepository.findById(id).map(RoomTypeView::from).orElseThrow(() -> new ResourceNotFoundException("Room type"));
    }
}
