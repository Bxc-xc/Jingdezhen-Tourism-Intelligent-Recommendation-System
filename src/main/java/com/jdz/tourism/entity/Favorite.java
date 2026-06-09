package com.jdz.tourism.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorite")
public class Favorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "scenic_id", nullable = false)
    private ScenicSpot scenicSpot;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    // 构造函数
    public Favorite() {}
    
    public Favorite(User user, ScenicSpot scenicSpot) {
        this.user = user;
        this.scenicSpot = scenicSpot;
        this.createTime = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public ScenicSpot getScenicSpot() {
        return scenicSpot;
    }
    
    public void setScenicSpot(ScenicSpot scenicSpot) {
        this.scenicSpot = scenicSpot;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}