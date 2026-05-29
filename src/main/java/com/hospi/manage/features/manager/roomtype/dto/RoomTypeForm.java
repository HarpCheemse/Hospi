package com.hospi.manage.features.manager.roomtype.dto;

import com.hospi.manage.features.manager.roomtype.enums.BedType;

import java.math.BigDecimal;

public class RoomTypeForm {
    private String name;

    private Short maxOccupancy;

    private String description;

    private String features;

    private BedType bedType;

    private Integer area;

    private String views;

    private BigDecimal basePrice;

    private Boolean isActive;

    public RoomTypeForm() {
    }

    public RoomTypeForm(String name, Short maxOccupancy, String description, String features, BedType bedType, Integer area, String views, BigDecimal basePrice, Boolean isActive) {
        this.name = name;
        this.maxOccupancy = maxOccupancy;
        this.description = description;
        this.features = features;
        this.bedType = bedType;
        this.area = area;
        this.views = views;
        this.basePrice = basePrice;
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Short getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(Short maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public BedType getBedType() {
        return bedType;
    }

    public void setBedType(BedType bedType) {
        this.bedType = bedType;
    }

    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
