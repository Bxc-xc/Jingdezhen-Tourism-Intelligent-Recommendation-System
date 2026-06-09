package com.jdz.tourism.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "reply")
public class Reply {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "replies", "user", "scenicSpot", "merchant", "marketplace"})
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "reviews", "reservations"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "scenicSpot", "activities", "roomTypes"})
    private Merchant merchant;

    @ManyToOne
    @JoinColumn(name = "parent_reply_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "childReplies", "review", "parentReply"})
    private Reply parentReply;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "parentReply", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reply> childReplies;
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 构造函数
    public Reply() {}
    
    public Reply(Review review, User user, String content) {
        this.review = review;
        this.user = user;
        this.content = content;
        this.createTime = LocalDateTime.now();
    }
    
    public Reply(Review review, Merchant merchant, String content) {
        this.review = review;
        this.merchant = merchant;
        this.content = content;
        this.createTime = LocalDateTime.now();
    }
    
    public Reply(Review review, User user, Reply parentReply, String content) {
        this.review = review;
        this.user = user;
        this.parentReply = parentReply;
        this.content = content;
        this.createTime = LocalDateTime.now();
    }
    
    public Reply(Review review, Merchant merchant, Reply parentReply, String content) {
        this.review = review;
        this.merchant = merchant;
        this.parentReply = parentReply;
        this.content = content;
        this.createTime = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Review getReview() {
        return review;
    }
    
    public void setReview(Review review) {
        this.review = review;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Merchant getMerchant() {
        return merchant;
    }
    
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }
    
    public Reply getParentReply() {
        return parentReply;
    }
    
    public void setParentReply(Reply parentReply) {
        this.parentReply = parentReply;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Reply> getChildReplies() {
        return childReplies;
    }
    
    public void setChildReplies(List<Reply> childReplies) {
        this.childReplies = childReplies;
    }
    
    /**
     * 判断回复者是否为商家
     */
    public boolean isMerchantReply() {
        return merchant != null;
    }
    
    /**
     * 获取回复者名称
     */
    public String getReplierName() {
        if (merchant != null) {
            return merchant.getShopName();
        } else if (user != null) {
            return user.getUsername();
        }
        return "匿名用户";
    }
}

