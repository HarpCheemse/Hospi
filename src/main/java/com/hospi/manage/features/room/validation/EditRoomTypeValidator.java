package com.hospi.manage.features.room.validation;

import com.hospi.manage.features.room.dto.request.RoomTypeEditForm;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validator for the room-type edit form ({@link RoomTypeEditForm}). */
@Component
public class EditRoomTypeValidator {

    private final RoomTypeRepository roomTypeRepository;
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg",
            "image/png",
            "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_ADDITIONAL_IMAGES = 5;

    public EditRoomTypeValidator(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    /**
     * Validate the room-type edit form for duplicates, cover image, gallery images, features, and price.
     *
     * @param roomTypeId        the id of the room type being edited
     * @param form              the edit form data
     * @param existingImageCount number of images already attached to the room type
     * @param errors            binding result to populate with validation failures
     */
    public void validate(Long roomTypeId, RoomTypeEditForm form, int existingImageCount, Errors errors) {
        validateDuplicateRoomType(roomTypeId,
                form,
                errors);
        validateCoverImage(form.coverImage(),
                errors);
        validateNewImages(form.newImages(),
                existingImageCount,
                errors);
        validateFeatures(form,
                errors);
        validatePrice(form,
                errors);
    }

    private void validateDuplicateRoomType(Long id, RoomTypeEditForm form, Errors errors) {

        if (errors.hasFieldErrors("category") || errors.hasFieldErrors("tier")) {
            return;
        }

        boolean exists = roomTypeRepository.existsByCategoryAndTierAndIdNotAndActiveTrue(form.category(),
                form.tier(),
                id);

        if (exists) {
            errors.rejectValue("tier",
                    "duplicate",
                    "This room category and tier already exists.");
        }
    }

    private void validateCoverImage(MultipartFile file, Errors errors) {
        if (file == null || file.isEmpty() || errors.hasFieldErrors("coverImage")) {
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            errors.rejectValue("coverImage",
                    "tooLarge",
                    "Image size must be less than 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            errors.rejectValue("coverImage",
                    "invalidType",
                    "Only JPG, PNG, and WEBP images are allowed.");
        }
    }

    private void validateNewImages(List<MultipartFile> newImages, int existingImageCount, Errors errors) {
        if (newImages == null || newImages.isEmpty()) {
            return;
        }

        int nonEmptyCount = (int) newImages.stream().filter(f -> f != null && !f.isEmpty()).count();

        if (nonEmptyCount + existingImageCount > MAX_ADDITIONAL_IMAGES) {
            errors.rejectValue("newImages",
                    "tooMany",
                    "Total images cannot exceed " + MAX_ADDITIONAL_IMAGES + ".");
        }

        for (int i = 0; i < newImages.size(); i++) {
            MultipartFile file = newImages.get(i);
            if (file == null || file.isEmpty()) continue;

            if (file.getSize() > MAX_FILE_SIZE) {
                errors.rejectValue("newImages[" + i + "]",
                        "tooLarge",
                        "Image size must be less than 5MB.");
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                errors.rejectValue("newImages[" + i + "]",
                        "invalidType",
                        "Only JPG, PNG, and WEBP images are allowed.");
            }
        }
    }

    private void validateFeatures(RoomTypeEditForm form, Errors errors) {
        int MAX_FEATURE_LENGTH = 20;

        String features = form.features();

        if (features == null || features.isBlank()) {
            return;
        }

        String[] items = features.split(",");

        for (String raw : items) {

            String item = raw.trim();

            if (item.isEmpty()) {
                errors.rejectValue("features",
                        "invalid",
                        "Features cannot contain empty values.");
                return;
            }

            if (!item.matches("^[a-zA-Z0-9 ]+$")) {
                errors.rejectValue("features",
                        "invalid",
                        "Features can only contain letters, numbers, and spaces.");
                return;
            }

            if (item.length() > MAX_FEATURE_LENGTH) {
                errors.rejectValue("features",
                        "invalid",
                        "Each feature must be " + MAX_FEATURE_LENGTH + " characters or less.");
                return;
            }
        }

        //Check for duplicate in features list
        Set<String> seen = new HashSet<>();

        for (String raw : items) {
            String item = raw.trim().toLowerCase();

            if (!seen.add(item)) {
                errors.rejectValue("features",
                        "duplicate",
                        "Duplicate feature: " + item);
                return;
            }
        }
    }

    private void validatePrice(RoomTypeEditForm form, Errors errors) {
        if (form.basePrice().scale() > 2) {
            errors.rejectValue("basePrice",
                    "invalid",
                    "Base price can only have 2 decimal places.");
        }
    }

}
