package com.jdz.tourism.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 旅游路线实体类
 */
@Entity
@Table(name = "travel_route")
public class TravelRoute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 500)
    private String image;
    
    @Column(name = "days")
    private Integer days;
    
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(length = 50)
    private String difficulty; // 轻松、中等、困难
    
    @Column(length = 200)
    private String tags; // 用逗号分隔的标签
    
    @Column(name = "scenic_spots", columnDefinition = "TEXT")
    private String scenicSpots; // JSON格式存储景点列表
    
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
    
    public Integer getDays() {
        return days;
    }
    
    public void setDays(Integer days) {
        this.days = days;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getScenicSpots() {
        return scenicSpots;
    }
    
    public void setScenicSpots(String scenicSpots) {
        this.scenicSpots = scenicSpots;
    }
}

