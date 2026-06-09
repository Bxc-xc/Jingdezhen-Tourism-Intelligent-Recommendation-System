package com.jdz.tourism.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "scenic_id")
    private ScenicSpot scenicSpot;
    
    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @ManyToOne
    @JoinColumn(name = "marketplace_id")
    private com.jdz.tourism.entity.Marketplace marketplace;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    @Column(nullable = false)
    private Integer rating;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    /**
     * 评价图片（逗号分隔的图片URL）
     */
    @Column(name = "images", columnDefinition = "TEXT")
    private String images;
    
    // 管理端/商家回复内容与时间（可选）
    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String replyContent;
    
    @Column(name = "reply_time")
    private LocalDateTime replyTime;
    
    // 软删除
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
    
    // 构造函数
    public Review() {}
    
    public Review(User user, ScenicSpot scenicSpot, String content, Integer rating) {
        this.user = user;
        this.scenicSpot = scenicSpot;
        this.content = content;
        this.rating = rating;
        this.createTime = LocalDateTime.now();
    }
    
    public Review(User user, Merchant merchant, String content, Integer rating) {
        this.user = user;
        this.merchant = merchant;
        this.content = content;
        this.rating = rating;
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public String getReplyContent() {
        return replyContent;
    }
    
    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }
    
    public LocalDateTime getReplyTime() {
        return replyTime;
    }
    
    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }
    
    public Merchant getMerchant() {
        return merchant;
    }
    
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public com.jdz.tourism.entity.Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(com.jdz.tourism.entity.Marketplace marketplace) {
        this.marketplace = marketplace;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}