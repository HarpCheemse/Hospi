package com.hospi.manage.features.room.validation;

import com.hospi.manage.features.room.dto.request.RoomTypeCreateForm;
import com.hospi.manage.features.room.repository.RoomTypeRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validator for the room-type creation form ({@link RoomTypeCreateForm}). */
@Component
public class CreateRoomTypeValidator {

    private final RoomTypeRepository roomTypeRepository;
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg",
            "image/png",
            "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public CreateRoomTypeValidator(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    /**
     * Validate the room-type creation form for duplicates, cover image, features, and price.
     *
     * @param form   the room-type creation form data
     * @param errors binding result to populate with validation failures
     */
    public void validate(RoomTypeCreateForm form, Errors errors) {
        validateDuplicateRoomType(form,
                errors);

        validateCoverImage(form.coverImage(),
                errors);
        validateFeatures(form,
                errors);

        validatePrice(form,
                errors);
    }

    private void validateDuplicateRoomType(RoomTypeCreateForm form, Errors errors) {
        if (errors.hasFieldErrors("category") || errors.hasFieldErrors("tier")) {
            return;
        }

        boolean exists = roomTypeRepository.existsByCategoryAndTierAndActiveTrue(form.category(),
                form.tier());
        if (exists) {
            errors.rejectValue("tier",
                    "duplicate",
                    "This room category and tier already exists.");
        }
    }

    private void validateFeatures(RoomTypeCreateForm form, Errors errors) {
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

    private void validateCoverImage(MultipartFile file, Errors errors) {
        if (errors.hasFieldErrors("coverImage") || file == null) {
            return;
        }

        if (file.isEmpty()) {
            errors.rejectValue("coverImage",
                    "empty",
                    "Uploaded image file is empty.");
            return;
        }

        // Validate File Size
        if (file.getSize() > MAX_FILE_SIZE) {
            errors.rejectValue("coverImage",
                    "tooLarge",
                    "Image size must be less than 5MB.");
        }

        // Validate File Content Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            errors.rejectValue("coverImage",
                    "invalidType",
                    "Only JPG, PNG, and WEBP images are allowed.");
        }
    }

    private void validatePrice(RoomTypeCreateForm form, Errors errors) {
        if (form.basePrice().scale() > 2) {
            errors.rejectValue("basePrice",
                    "invalid",
                    "Base price can only have 2 decimal places.");
        }
    }


}