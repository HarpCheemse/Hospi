package com.hospi.manage.features.manager.roomtype.service;

import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.utils.ImageUtils;
import com.hospi.manage.features.manager.roomtype.dto.RoomTypeForm;
import com.hospi.manage.features.manager.roomtype.entity.RoomType;
import com.hospi.manage.features.manager.roomtype.entity.RoomTypePicture;
import com.hospi.manage.features.manager.roomtype.repository.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    @Transactional
    public void create(RoomTypeForm form, MultipartFile image) throws IOException {
        RoomType roomType = new RoomType();

        roomType.setName(form.getName());
        roomType.setMaxOccupancy(form.getMaxOccupancy());
        roomType.setDescription(form.getDescription());
        roomType.setFeatures(form.getFeatures());
        roomType.setBedType(form.getBedType().toString());
        roomType.setArea(form.getArea());
        roomType.setBasePrice(form.getBasePrice());
        roomType.setActive(true);

        byte[] compressed = ImageUtils.compressWebP(image, 360, 0.75f);

        RoomTypePicture picture = new RoomTypePicture();
        picture.setRoomType(roomType);
        picture.setImageData(compressed);

        roomType.getPictures().add(picture);

        roomTypeRepository.save(roomType);
    }

    public List<RoomType> findAll() {
        return roomTypeRepository.findAll();
    }

    public RoomType findById(Long id) {
        return roomTypeRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Room type not found"));
    }

    @Transactional
    public void update(Long id, RoomType form, MultipartFile images) throws IOException{
        RoomType roomType = findById(id);

        roomType.setName(form.getName());
        roomType.setBedType(form.getBedType());
        roomType.setMaxOccupancy(form.getMaxOccupancy());
        roomType.setArea(form.getArea());
        roomType.setBasePrice(form.getBasePrice());
        roomType.setDescription(form.getDescription());
        roomType.setFeatures(form.getFeatures());
        roomType.setActive(form.getActive());

        //ONLY 1 images for now
        if (images != null && !images.isEmpty()) {
            byte[] compressed = ImageUtils.compressWebP(images, 360, 0.75f);
            RoomTypePicture picture = new RoomTypePicture();
            picture.setRoomType(roomType);
            picture.setImageData(compressed);

            roomType.getPictures().clear();
            roomType.getPictures().add(picture);
        }
        roomTypeRepository.save(roomType);
    }

    @Transactional
    public void delete(Long id){
        RoomType roomType = findById(id);
        roomTypeRepository.delete(roomType);
    }
}
