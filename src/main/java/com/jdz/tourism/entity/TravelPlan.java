package com.jdz.tourism.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "travel_plan")
public class TravelPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer days;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;
    
    @Column(name = "budget_type", length = 20)
    private String budgetType; // low, medium, high
    
    @Column(name = "transport_type", length = 20)
    private String transportType; // car, bus, train, plane
    
    @Column(columnDefinition = "TEXT")
    private String interests; // 兴趣标签，JSON格式
    
    @Column(columnDefinition = "TEXT")
    private String planDetails; // 行程详情，JSON格式
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 构造函数
    public TravelPlan() {}
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    public TravelPlan(User user, String title, String description, Integer days, 
                     LocalDate startDate, LocalDate endDate, BigDecimal totalCost) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.days = days;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
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
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getDays() {
        return days;
    }
    
    public void setDays(Integer days) {
        this.days = days;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public String getBudgetType() {
        return budgetType;
    }
    
    public void setBudgetType(String budgetType) {
        this.budgetType = budgetType;
    }
    
    public String getTransportType() {
        return transportType;
    }
    
    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }
    
    public String getInterests() {
        return interests;
    }
    
    public void setInterests(String interests) {
        this.interests = interests;
    }
    
    public String getPlanDetails() {
        return planDetails;
    }
    
    public void setPlanDetails(String planDetails) {
        this.planDetails = planDetails;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}