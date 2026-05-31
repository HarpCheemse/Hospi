package com.hospi.manage.features.manager.detail.service;

import com.hospi.manage.common.constant.HotelConstants;
import com.hospi.manage.common.exception.ResourceNotFoundException;
import com.hospi.manage.common.utils.ImageUtils;
import com.hospi.manage.features.manager.detail.dto.HotelForm;
import com.hospi.manage.features.manager.detail.entity.Hotel;
import com.hospi.manage.features.manager.detail.entity.HotelPicture;
import com.hospi.manage.features.manager.detail.repository.HotelRepository;
import com.hospi.manage.features.manager.roomtype.entity.RoomTypePicture;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;

    HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    /// There is only 1 hotel so find by id should pass in null
    public Hotel findById(Long id) {
        return hotelRepository.findById(HotelConstants.HOTEL_ID).orElseThrow(
                () -> new ResourceNotFoundException("Hotel not found")
        );
    }

    public HotelForm getForm(Long id) {
        Hotel hotel = findById(null);
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
        form.setTermOfService(hotel.getTermOfService());

        return form;
    }

    @Transactional
    public void update(HotelForm form, MultipartFile images)
            throws IOException {
        Hotel hotel = findById(null);

        hotel.setName(form.getName());
        hotel.setDescription(form.getDescription());
        hotel.setAddress(form.getAddress());
        hotel.setPhone(form.getPhone());
        hotel.setEmail(form.getEmail());
        hotel.setCheckInTime(form.getCheckInTime());
        hotel.setCheckOutTime(form.getCheckOutTime());
        hotel.setFeatures(form.getFeatures());
        hotel.setStatus(form.getStatus());
        hotel.setTermOfService(form.getTermOfService());

        if (images != null && !images.isEmpty()) {
            byte[] compressed = ImageUtils.compressWebP(images, 720, 0.75f);
            HotelPicture picture = new HotelPicture();
            picture.setHotel(hotel);
            picture.setImageData(compressed);

            //This is temporary solution due to only have 1 picture per hotel
            hotel.getPictures().clear();
            hotel.getPictures().add(picture);
            System.out.println(hotel.getPictures().size());
        }
        hotelRepository.save(hotel);
    }
}
