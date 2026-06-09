package com.jdz.tourism.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "merchant")
public class Merchant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @NotBlank(message = "店铺名称不能为空")
    @Column(nullable = false, length = 100)
    private String shopName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 商家分类：FOOD(美食), CERAMIC(陶瓷体验), HOTEL(酒店), SCENIC(景点), OTHER(其他)
     */
    @Column(length = 50)
    private String category;

    /**
     * 关联的景点ID（仅当category=SCENIC时使用）
     * 用于将景点归纳到商家管理，同时保留景点数据的独立性
     */
    @ManyToOne
    @JoinColumn(name = "scenic_id")
    private ScenicSpot scenicSpot;

    /**
     * 店铺地址（可选）
     */
    @Column(length = 255)
    private String address;

    /**
     * 联系电话（可选）
     */
    @Column(length = 50)
    private String phone;

    /**
     * 营业时间（可选，例如：9:00-18:00）
     */
    @Column(length = 100)
    private String openTime;

    /**
     * 起步价 / 人均消费 / 门票价格
     */
    @Column(name = "start_price", precision = 10, scale = 2)
    private java.math.BigDecimal startPrice;

    /**
     * 管理员评分（如果不为空，优先展示此评分）
     */
    @Column(name = "admin_rating")
    private Double adminRating;

    /**
     * 店铺头像 / 主图（可选，存储图片 URL）
     */
    @Column(length = 500)
    private String avatar;

    /**
     * 店铺图片集合（可选，多张图片的URL，以逗号分隔存储）
     * 说明：
     * - 第一张图片默认为酒店卡片封面
     * - 其余图片用于酒店详情页轮播展示
     */
    @Column(name = "shop_images", columnDefinition = "TEXT")
    private String shopImages;

    /**
     * 餐饮类型（仅当category=FOOD时使用）
     * 可选值：SNACK(小吃), BEVERAGE(奶茶饮品), DESSERT(甜品), FAST_FOOD(快餐),
     * HOTPOT(火锅), BBQ(烧烤), NOODLE(面食), BAKERY(烘焙), COFFEE(咖啡),
     * LIGHT_MEAL(轻食), LOCAL(地方特色), OTHER(其他)
     */
    @Column(name = "cuisine_type", length = 50)
    private String cuisineType;

    /**
     * 人均消费（仅当category=FOOD时使用，例如：50-100）
     */
    @Column(name = "avg_price", length = 50)
    private String avgPrice;

    /**
     * 酒店星级（仅当category=HOTEL时使用）
     * 可选值：ECONOMY(经济型), 3_STAR(三星级), 4_STAR(四星级), 5_STAR(五星级), LUXURY(豪华型)
     */
    @Column(name = "star_rating", length = 50)
    private String starRating;

    /**
     * 房间数量（仅当category=HOTEL时使用）
     */
    @Column(name = "room_count")
    private Integer roomCount;

    /**
     * 商家标签（多个标签以逗号分隔，如：位置优越,亲子友好）
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /**
     * 酒店设施（多个设施以逗号分隔，如：免费WiFi,停车场）
     */
    @Column(name = "facilities", columnDefinition = "TEXT")
    private String facilities;

    /**
     * 启用状态：true=启用（对前台可见），false=停用（对前台不可见）
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 逻辑删除标记：true=已删除（对前台不可见）
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 删除时间（逻辑删除时写入）
     */
    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    @Transient
    private java.util.List<java.util.Map<String, Object>> activities;
    
    // 构造函数
    public Merchant() {}
    
    public Merchant(User user, String shopName, String description) {
        this.user = user;
        this.shopName = shopName;
        this.description = description;
    }
    
    public Merchant(User user, String shopName, String description, String category) {
        this.user = user;
        this.shopName = shopName;
        this.description = description;
        this.category = category;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getShopName() {
        return shopName;
    }
    
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
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

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public java.math.BigDecimal getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(java.math.BigDecimal startPrice) {
        this.startPrice = startPrice;
    }

    public Double getAdminRating() {
        return adminRating;
    }

    public void setAdminRating(Double adminRating) {
        this.adminRating = adminRating;
    }

    /**
     * 兼容旧代码中对 rating 字段的访问，内部复用 adminRating。
     */
    public Double getRating() {
        return adminRating;
    }

    public void setRating(Double rating) {
        this.adminRating = rating;
    }

    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getShopImages() {
        return shopImages;
    }

    public void setShopImages(String shopImages) {
        this.shopImages = shopImages;
    }

    public ScenicSpot getScenicSpot() {
        return scenicSpot;
    }

    public void setScenicSpot(ScenicSpot scenicSpot) {
        this.scenicSpot = scenicSpot;
    }

    /**
     * 判断是否为景点类型的商家
     */
    public boolean isScenicMerchant() {
        return "SCENIC".equals(category) && scenicSpot != null;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(String avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getStarRating() {
        return starRating;
    }

    public void setStarRating(String starRating) {
        this.starRating = starRating;
    }

    public Integer getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(Integer roomCount) {
        this.roomCount = roomCount;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public java.time.LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(java.time.LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}