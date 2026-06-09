package com.jdz.tourism.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Column(nullable = false, length = 100)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.TOURIST;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Column(length = 20)
    private String phone;
    
    /**
     * 用户邮箱（可选）
     */
    @Column(length = 100)
    private String email;
    
    /**
     * 个人简介（可选）
     */
    @Column(length = 500)
    private String bio;
    
    /**
     * 用户头像（可选，存储图片 URL）
     */
    @Column(length = 500)
    private String avatar;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 在持久化之前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        // 使用Asia/Shanghai时区确保时间准确
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
        // 使用Asia/Shanghai时区确保时间准确
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        updatedAt = LocalDateTime.now(zoneId);
    }
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public User() {}
    
    public User(String username, String password, UserRole role, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.phone = phone;
        // 使用Asia/Shanghai时区确保时间准确
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        this.createdAt = LocalDateTime.now(zoneId);
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
    
    // 用户角色枚举
    public enum UserRole {
        TOURIST, MERCHANT, ADMIN
    }
}