package com.jdz.tourism.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservation")
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @NotNull(message = "商家ID不能为空")
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    
    @NotNull(message = "景点ID不能为空")
    @Column(name = "scenic_id", nullable = false)
    private Long scenicId;
    
    @NotNull(message = "预约日期不能为空")
    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;
    
    @NotNull(message = "预约时间不能为空")
    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;
    
    @Min(value = 1, message = "参与人数至少为1人")
    @Column(nullable = false)
    private Integer participants = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String remark;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    public enum ReservationStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED
    }
    
    // 构造函数
    public Reservation() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }
    
    public Reservation(Long userId, Long merchantId, Long scenicId, 
                      LocalDate reservationDate, LocalTime reservationTime, 
                      Integer participants, String remark) {
        this();
        this.userId = userId;
        this.merchantId = merchantId;
        this.scenicId = scenicId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.participants = participants;
        this.remark = remark;
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
    
    public Long getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    public Long getScenicId() {
        return scenicId;
    }
    
    public void setScenicId(Long scenicId) {
        this.scenicId = scenicId;
    }
    
    public LocalDate getReservationDate() {
        return reservationDate;
    }
    
    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }
    
    public LocalTime getReservationTime() {
        return reservationTime;
    }
    
    public void setReservationTime(LocalTime reservationTime) {
        this.reservationTime = reservationTime;
    }
    
    public Integer getParticipants() {
        return participants;
    }
    
    public void setParticipants(Integer participants) {
        this.participants = participants;
    }
    
    public ReservationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
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