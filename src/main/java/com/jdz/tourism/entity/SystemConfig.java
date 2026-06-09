package com.jdz.tourism.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "system_config")
public class SystemConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 配置键（唯一）
     */
    @Column(unique = true, nullable = false, length = 100)
    private String configKey;
    
    /**
     * 配置值
     */
    @Column(columnDefinition = "TEXT")
    private String configValue;
    
    /**
     * 配置描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 在持久化之前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        if (createdAt == null) {
            createdAt = LocalDateTime.now(zoneId);
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now(zoneId);
        }
    }
    
    /**
     * 在更新之前自动设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        updatedAt = LocalDateTime.now(zoneId);
    }
    
    // 构造函数
    public SystemConfig() {}
    
    public SystemConfig(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        this.createdAt = LocalDateTime.now(zoneId);
        this.updatedAt = LocalDateTime.now(zoneId);
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getConfigKey() {
        return configKey;
    }
    
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
    
    public String getConfigValue() {
        return configValue;
    }
    
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

