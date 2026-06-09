package com.jdz.tourism.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Entity
@Table(name = "scenic_spot")
public class ScenicSpot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "景点名称不能为空")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @DecimalMin(value = "0.0", message = "价格不能为负数")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(length = 50)
    private String openTime;
    
    @Column(length = 50)
    private String category;
    
    @Column(length = 100)
    private String tags;
    
    @Column(name = "visit_count", nullable = false)
    private Long visitCount = 0L;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String image;

    /**
     * 景点图片集合（多张图片的URL，以逗号分隔存储）
     * 说明：
     * - 第一张图片默认为景点封面
     * - 其余图片用于景点详情页轮播展示
     */
    @Column(name = "scenic_images", columnDefinition = "TEXT")
    private String scenicImages;

    /**
     * 景点评分（0-5分，支持小数）
     * 如果为null，则从用户评论自动计算平均评分
     * 管理员可以手动设置评分，手动设置的评分优先于自动计算的评分
     */
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Transient
    private String images; // 兼容前端期望的复数字段

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String phone;

    /**
     * 官方电话；与 phone 区分，供前端“官方电话”单独展示
     */
    @Column(name = "official_phone", length = 50)
    private String officialPhone;

    /**
     * 开放时间详细说明，用于“全年 08:30-22:00（21:30停止入园）”等展示
     */
    @Column(name = "opening_hours_detail", length = 255)
    private String openingHoursDetail;

    /**
     * 公告（短文案）
     */
    @Column(name = "notice", length = 255)
    private String notice;

    /**
     * 公告详情/公告信息（长文案）
     */
    @Column(name = "notice_detail", columnDefinition = "TEXT")
    private String noticeDetail;

    /**
     * 优待政策（如：学生半价、老人免票等）
     */
    @Column(name = "preferential_policy", columnDefinition = "TEXT")
    private String preferentialPolicy;

    /**
     * 服务设施详情
     */
    @Column(name = "facilities_detail", columnDefinition = "TEXT")
    private String facilitiesDetail;

    /**
     * 门票信息（如：2026年中国100家核心卡点）
     */
    @Column(name = "ticket_info", length = 255)
    private String ticketInfo;

    /**
     * 景点特色（逗号分隔，如：陶瓷文化,非遗体验,亲子游）
     */
    @Column(name = "features", columnDefinition = "TEXT")
    private String features;

    /**
     * 亮点推荐（逗号分隔，如：千年窑火,匠心传承）
     */
    @Column(name = "highlights", columnDefinition = "TEXT")
    private String highlights;

    /**
     * 游玩提示（如：建议游览时间2-3小时，旺季请提前购票）
     */
    @Column(name = "tips", columnDefinition = "TEXT")
    private String tips;

    @Transient
    private Boolean qualificationApproved; // 商家资质审核状态

    @Transient
    private java.util.List<java.util.Map<String, Object>> activities;
    
    @Transient
    private User user; // 关联的商家账号信息

    /**
     * 点评数量（动态统计，不入库）
     */
    @Transient
    private Long commentCount;
    
    // 构造函数
    public ScenicSpot() {}
    
    public ScenicSpot(String name, String description, BigDecimal price, String openTime, String category, String tags) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.openTime = openTime;
        this.category = category;
        this.tags = tags;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getOpenTime() {
        return openTime;
    }
    
    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Long getVisitCount() {
        return visitCount;
    }
    
    public void setVisitCount(Long visitCount) {
        this.visitCount = visitCount;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getScenicImages() {
        return scenicImages;
    }

    public void setScenicImages(String scenicImages) {
        this.scenicImages = scenicImages;
    }

    public String getImages() {
        // 优先返回 scenicImages（多张图片），如果没有则回退为单张 image
        if (scenicImages != null && !scenicImages.isBlank()) {
            return scenicImages;
        }
        if ((images == null || images.isBlank()) && image != null) {
            return image;
        }
        return images;
    }

    public void setImages(String images) {
        this.images = images;
        // 如果设置了 images，同时更新 scenicImages
        if (images != null && !images.isBlank()) {
            this.scenicImages = images;
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOfficialPhone() {
        return officialPhone;
    }

    public void setOfficialPhone(String officialPhone) {
        this.officialPhone = officialPhone;
    }

    public String getOpeningHoursDetail() {
        return openingHoursDetail;
    }

    public void setOpeningHoursDetail(String openingHoursDetail) {
        this.openingHoursDetail = openingHoursDetail;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getNoticeDetail() {
        return noticeDetail;
    }

    public void setNoticeDetail(String noticeDetail) {
        this.noticeDetail = noticeDetail;
    }

    public Boolean getQualificationApproved() {
        return qualificationApproved;
    }

    public void setQualificationApproved(Boolean qualificationApproved) {
        this.qualificationApproved = qualificationApproved;
    }

    public java.util.List<java.util.Map<String, Object>> getActivities() {
        return activities;
    }

    public void setActivities(java.util.List<java.util.Map<String, Object>> activities) {
        this.activities = activities;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public String getPreferentialPolicy() {
        return preferentialPolicy;
    }

    public void setPreferentialPolicy(String preferentialPolicy) {
        this.preferentialPolicy = preferentialPolicy;
    }

    public String getFacilitiesDetail() {
        return facilitiesDetail;
    }

    public void setFacilitiesDetail(String facilitiesDetail) {
        this.facilitiesDetail = facilitiesDetail;
    }

    public String getTicketInfo() {
        return ticketInfo;
    }

    public void setTicketInfo(String ticketInfo) {
        this.ticketInfo = ticketInfo;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getHighlights() {
        return highlights;
    }

    public void setHighlights(String highlights) {
        this.highlights = highlights;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}