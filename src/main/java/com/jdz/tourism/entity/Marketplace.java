package com.jdz.tourism.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace")
public class Marketplace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "市集名称不能为空")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 开放时间/营业时间（例如：9:00-18:00 / 全天开放）
     */
    @Column(length = 100)
    private String openTime;

    /**
     * 地址
     */
    @Column(length = 255)
    private String address;

    /**
     * 详细介绍
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 轮播图图片URL集合（逗号分隔）
     */
    @Column(name = "carousel_images", columnDefinition = "TEXT")
    private String carouselImages;

    /**
     * 列表封面图（可选；不填则默认取轮播图第一张）
     */
    @Column(name = "cover_image", length = 500)
    private String coverImage;

    /**
     * 排序（数字越小越靠前）
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否启用（游客端只展示启用的数据）
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "latitude", precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.enabled == null) this.enabled = true;
        if (this.sortOrder == null) this.sortOrder = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.enabled == null) this.enabled = true;
        if (this.sortOrder == null) this.sortOrder = 0;
    }

    public Marketplace() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCarouselImages() {
        return carouselImages;
    }

    public void setCarouselImages(String carouselImages) {
        this.carouselImages = carouselImages;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public java.math.BigDecimal getLatitude() { return latitude; }
    public void setLatitude(java.math.BigDecimal latitude) { this.latitude = latitude; }
    public java.math.BigDecimal getLongitude() { return longitude; }
    public void setLongitude(java.math.BigDecimal longitude) { this.longitude = longitude; }
}


