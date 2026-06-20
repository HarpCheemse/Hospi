package com.hospi.manage.features.room.dto.request;

import com.hospi.manage.features.room.enums.BedType;
import com.hospi.manage.features.room.enums.RoomCategory;
import com.hospi.manage.features.room.enums.RoomTier;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record RoomTypeCreateForm(
        @NotNull(message = "Room category is required")
        RoomCategory category,

        @NotNull(message = "Room tier is required")
        RoomTier tier,

        @NotNull(message = "Max occupancy is required")
        @Min(value = 1, message = "Max occupancy must be at least 1")
        @Max(value = 20, message = "Max occupancy is too large")
        Integer maxOccupancy,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description is too long")
        String description,

        @NotBlank(message = "Features are required")
        String features,

        @NotNull(message = "Bed type is required")
        BedType bedType,

        @NotNull(message = "Area is required")
        @Min(value = 1, message = "Area must be greater than 0")
        @Max(value = 200, message = "Area must be smaller than 200")
        Integer area,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
        BigDecimal basePrice,

        @NotNull(message = "Cover image is required")
        MultipartFile coverImage
) {
    public static RoomTypeCreateForm empty() {
        return new RoomTypeCreateForm(
                null,
                null,
                null,
                null,
                "",
                null,
                null,
                null,
                null
        );
    }
}
