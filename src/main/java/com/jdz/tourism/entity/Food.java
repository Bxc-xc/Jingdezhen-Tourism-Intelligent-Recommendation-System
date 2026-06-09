package com.jdz.tourism.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 美食实体类
 */
@Entity
@Table(name = "food")
public class Food {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 500)
    private String image;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;
    
    @Column(name = "review_count")
    private Integer reviewCount;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(length = 200)
    private String address;
    
    @Column(length = 50)
    private String category;
    
    @Column(length = 200)
    private String tags; // 用逗号分隔的标签
    
    @Transient
    private java.util.List<java.util.Map<String, Object>> activities;

    // Getters and Setters
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public BigDecimal getRating() {
        return rating;
    }
    
    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }
    
    public Integer getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }

    public java.util.List<java.util.Map<String, Object>> getActivities() {
        return activities;
    }

    public void setActivities(java.util.List<java.util.Map<String, Object>> activities) {
        this.activities = activities;
    }
}

