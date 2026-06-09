package com.jdz.tourism.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;
    
    // 景点可以为空，方便支持纯商家预约（如陶艺体验等）
    @ManyToOne
    @JoinColumn(name = "scenic_id")
    private ScenicSpot scenicSpot;
    
    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    // 预约时段（例如：2025-01-01 14:00-16:00），可选
    @Column(name = "reservation_time_slot")
    private String reservationTimeSlot;

    // 预约备注信息，可选
    @Column(name = "reservation_note", length = 500)
    private String reservationNote;

    // 房型关联
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    // 入住日期
    @Column(name = "check_in_date")
    private java.time.LocalDate checkInDate;

    // 离店日期
    @Column(name = "check_out_date")
    private java.time.LocalDate checkOutDate;
    
    // 房间数量
    private Integer quantity;
    
    // 总价
    @Column(name = "total_price", precision = 10, scale = 2)
    private java.math.BigDecimal totalPrice;

    public Order() {
    }

    public Order(User user, Merchant merchant, ScenicSpot scenicSpot, LocalDateTime orderTime) {
        this.user = user;
        this.merchant = merchant;
        this.scenicSpot = scenicSpot;
        this.orderTime = orderTime;
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
    
    public Merchant getMerchant() {
        return merchant;
    }
    
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }
    
    public ScenicSpot getScenicSpot() {
        return scenicSpot;
    }
    
    public void setScenicSpot(ScenicSpot scenicSpot) {
        this.scenicSpot = scenicSpot;
    }
    
    public LocalDateTime getOrderTime() {
        return orderTime;
    }
    
    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getReservationTimeSlot() {
        return reservationTimeSlot;
    }

    public void setReservationTimeSlot(String reservationTimeSlot) {
        this.reservationTimeSlot = reservationTimeSlot;
    }

    public String getReservationNote() {
        return reservationNote;
    }

    public void setReservationNote(String reservationNote) {
        this.reservationNote = reservationNote;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public java.time.LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(java.time.LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public java.time.LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(java.time.LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public java.math.BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(java.math.BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    // 订单状态枚举
    public enum OrderStatus {
        PENDING, CONFIRMED, CANCELLED
    }
}