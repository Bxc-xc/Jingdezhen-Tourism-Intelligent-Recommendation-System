package com.jdz.tourism.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_buy")
public class GroupBuy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 512)
    private String image;

    @Column(name = "group_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal groupPrice;

    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false)
    private Integer stock = 100;

    @Column(name = "sold_count", nullable = false)
    private Integer soldCount = 0;

    @Column(name = "usage_desc", length = 500)
    private String usageDesc;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "valid_start")
    private LocalDate validStart;

    @Column(name = "valid_end", nullable = false)
    private LocalDate validEnd;

    /**
     * PENDING / APPROVED / REJECTED / OFFLINE
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public BigDecimal getGroupPrice() { return groupPrice; }
    public void setGroupPrice(BigDecimal groupPrice) { this.groupPrice = groupPrice; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getSoldCount() { return soldCount; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }

    public String getUsageDesc() { return usageDesc; }
    public void setUsageDesc(String usageDesc) { this.usageDesc = usageDesc; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public LocalDate getValidStart() { return validStart; }
    public void setValidStart(LocalDate validStart) { this.validStart = validStart; }

    public LocalDate getValidEnd() { return validEnd; }
    public void setValidEnd(LocalDate validEnd) { this.validEnd = validEnd; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
