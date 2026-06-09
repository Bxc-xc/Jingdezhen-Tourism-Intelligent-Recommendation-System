package com.jdz.tourism.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 行程版本快照表，用于支持行程的版本管理和回滚
 */
@Entity
@Table(name = "travel_plan_version")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "plan"})
public class TravelPlanVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属行程
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private TravelPlan plan;

    /**
     * 版本号，从 1 开始递增
     */
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    /**
     * 版本快照时的标题
     */
    @Column(length = 100)
    private String title;

    /**
     * 版本快照时的描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 行程详情 JSON 快照
     */
    @Column(name = "plan_details", columnDefinition = "TEXT")
    private String planDetails;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TravelPlan getPlan() {
        return plan;
    }

    public void setPlan(TravelPlan plan) {
        this.plan = plan;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
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
}


