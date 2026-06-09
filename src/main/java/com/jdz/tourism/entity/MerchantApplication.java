package com.jdz.tourism.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "merchant_application")
@org.hibernate.annotations.DynamicUpdate
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MerchantApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User user;
    
    @NotBlank(message = "营业执照不能为空")
    @Column(name = "business_license", columnDefinition = "LONGTEXT", nullable = false)
    private String businessLicense;
    
    @NotBlank(message = "身份证不能为空")
    @Column(name = "identity_card", columnDefinition = "LONGTEXT", nullable = false)
    private String identityCard;
    
    @Column(name = "shop_photos", columnDefinition = "TEXT")
    private String shopPhotos;
    
    @NotBlank(message = "申请描述不能为空")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING'")
    private ApplicationStatus status = ApplicationStatus.PENDING;
    
    @Column(name = "audit_opinion", columnDefinition = "TEXT")
    private String auditOpinion;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    public enum ApplicationStatus {
        PENDING, APPROVED, REJECTED
    }
    
    // 构造函数
    public MerchantApplication() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }
    
    public MerchantApplication(Long userId, String businessLicense, String identityCard, 
                             String shopPhotos, String description) {
        this();
        this.userId = userId;
        this.businessLicense = businessLicense;
        this.identityCard = identityCard;
        this.shopPhotos = shopPhotos;
        this.description = description;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public String getBusinessLicense() {
        return businessLicense;
    }
    
    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }
    
    public String getIdentityCard() {
        return identityCard;
    }
    
    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }
    
    public String getShopPhotos() {
        return shopPhotos;
    }
    
    public void setShopPhotos(String shopPhotos) {
        this.shopPhotos = shopPhotos;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    public String getAuditOpinion() {
        return auditOpinion;
    }
    
    public void setAuditOpinion(String auditOpinion) {
        this.auditOpinion = auditOpinion;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}