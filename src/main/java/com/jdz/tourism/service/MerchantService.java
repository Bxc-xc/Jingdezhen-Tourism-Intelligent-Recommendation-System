package com.jdz.tourism.service;

import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.ScenicSpot; // Added import
import com.jdz.tourism.entity.User;
import com.jdz.tourism.entity.MerchantApplication;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.repository.MerchantApplicationRepository;
import com.jdz.tourism.repository.MerchantActivityRepository;
import com.jdz.tourism.repository.OrderRepository;
import com.jdz.tourism.repository.ReviewRepository;
import com.jdz.tourism.repository.GroupBuyOrderRepository;
import com.jdz.tourism.repository.GroupBuyRepository;
import com.jdz.tourism.entity.MerchantActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class MerchantService {
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired(required = false)
    @Lazy
    private ReviewService reviewService;
    
    @Autowired(required = false)
    private MerchantApplicationRepository merchantApplicationRepository;

    @Autowired
    private MerchantActivityRepository merchantActivityRepository;

    @Autowired
    private RealtimeDataService realtimeDataService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private com.jdz.tourism.repository.RoomTypeRepository roomTypeRepository;

    @Autowired
    private com.jdz.tourism.repository.MerchantFavoriteRepository merchantFavoriteRepository;

    @Autowired(required = false)
    private SystemConfigService systemConfigService;

    @org.springframework.beans.factory.annotation.Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    /** 删除 uploads/ 目录下的图片文件（URL 格式：/uploads/xxx.jpg） */
    private void deleteUploadedFiles(String... urls) {
        for (String url : urls) {
            if (url == null || url.isBlank()) continue;
            // 支持逗号分隔的多图
            for (String u : url.split(",")) {
                u = u.trim();
                if (u.isEmpty()) continue;
                try {
                    // 从 URL 提取文件名：/uploads/abc.jpg → abc.jpg
                    String filename = u.replaceAll("^https?://[^/]+", "")
                                       .replaceFirst("^/uploads/", "");
                    if (filename.contains("/") || filename.isBlank()) continue;
                    java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir, filename);
                    if (java.nio.file.Files.exists(filePath)) {
                        java.nio.file.Files.delete(filePath);
                        System.out.println("已删除文件: " + filePath);
                    }
                } catch (Exception e) {
                    System.err.println("删除文件失败: " + u + " (" + e.getMessage() + ")");
                }
            }
        }
    }
    @Autowired
    private GroupBuyOrderRepository groupBuyOrderRepository;

    @Autowired
    private GroupBuyRepository groupBuyRepository;

    
    /**
     * 创建商家
     */
    public Merchant createMerchant(Long userId, String shopName, String description) {
        return createMerchant(userId, shopName, description, "OTHER");
    }
    
    /**
     * 创建商家（带分类）
     */
    public Merchant createMerchant(Long userId, String shopName, String description, String category) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查用户是否已经是商家
        if (merchantRepository.findByUserAndDeletedFalse(user).isPresent()) {
            throw new RuntimeException("该用户已经是商家");
        }
        
        // 检查店铺名称是否已存在
        if (merchantRepository.findByShopNameAndDeletedFalse(shopName).isPresent()) {
            throw new RuntimeException("店铺名称已存在");
        }
        
        // 创建商家（默认禁用，需通过资质审核后才能上架）
        Merchant merchant = new Merchant();
        merchant.setUser(user);
        merchant.setShopName(shopName);
        merchant.setDescription(description);
        merchant.setCategory(category != null ? category : "OTHER");
        merchant.setEnabled(false);
        merchant.setDeleted(false);
        merchant.setDeletedAt(null);
        
        return merchantRepository.save(merchant);
    }
    

    /**
     * 为景点创建关联的商家（如果不存在）
     */
    public Merchant createMerchantForScenicSpot(ScenicSpot scenicSpot) {
        Optional<Merchant> existing = merchantRepository.findByScenicSpotId(scenicSpot.getId());
        if (existing.isPresent()) {
            return existing.get();
        }
        
        Merchant merchant = new Merchant();
        merchant.setShopName(scenicSpot.getName());
        merchant.setDescription(scenicSpot.getDescription());
        merchant.setCategory("SCENIC");
        merchant.setScenicSpot(scenicSpot);
        merchant.setOpenTime(scenicSpot.getOpenTime());
        merchant.setStartPrice(scenicSpot.getPrice());
        merchant.setShopImages(scenicSpot.getScenicImages());
        // 如果景点有图片，使用第一张作为头像
        if (scenicSpot.getImage() != null) {
            merchant.setAvatar(scenicSpot.getImage());
        }
        
        // 注意：这里没有设置User，因为是系统自动生成的关联商家
        
        return merchantRepository.save(merchant);
    }
    
    /**
     * 根据ID获取商家详情
     * 支持商家ID和景点ID（如果是景点类型的商家）
     * 先尝试按商家ID查找，如果找不到，再尝试按景点ID查找
     */
    public Optional<Merchant> getMerchantById(Long id) {
        // 先尝试按商家ID查找
        Optional<Merchant> merchantOpt = merchantRepository.findActiveById(id);
        if (merchantOpt.isPresent()) {
            return merchantOpt;
        }
        // 如果找不到，尝试按景点ID查找（用于景点类型的商家）
        return merchantRepository.findByScenicSpotId(id);
    }
    
    /**
     * 根据用户ID获取商家
     */
    public Optional<Merchant> getMerchantByUserId(Long userId) {
        return merchantRepository.findByUserIdAndDeletedFalse(userId);
    }
    
    /**
     * 根据店铺名称获取商家
     */
    public Optional<Merchant> getMerchantByShopName(String shopName) {
        return merchantRepository.findByShopNameAndDeletedFalse(shopName);
    }
    
    /**
     * 更新商家信息
     */
    public Merchant updateMerchant(Merchant merchant) {
        Merchant updated = merchantRepository.save(merchant);
        
        // 推送实时更新
        try {
            // 使用enrichMerchantWithRating构建完整的数据结构，包含图片数组等
            Map<String, Object> enrichedData = enrichMerchantWithRating(updated);
            System.out.println("Pushing Merchant Update: ID=" + enrichedData.get("id") + ", Images=" + enrichedData.get("shopImages"));
            realtimeDataService.pushMerchantUpdate(enrichedData, "update");
        } catch (Exception e) {
            System.err.println("推送商家实时更新失败: " + e.getMessage());
        }
        
        return updated;
    }
    
    /**
     * 根据景点ID获取关联的商家
     */
    public Optional<Merchant> getMerchantByScenicSpotId(Long scenicSpotId) {
        System.out.println("========== Service层: 查询景点ID " + scenicSpotId + " 对应的商家 ==========");
        try {
            Optional<Merchant> result = merchantRepository.findByScenicSpotId(scenicSpotId);
            if (result.isPresent()) {
                Merchant m = result.get();
                System.out.println("Service层: 找到商家 ID=" + m.getId() + ", shopName=" + m.getShopName() + ", scenic_id=" + (m.getScenicSpot() != null ? m.getScenicSpot().getId() : "null"));
            } else {
                System.out.println("Service层: 未找到景点ID " + scenicSpotId + " 对应的商家");
                System.out.println("Service层: 尝试直接查询数据库...");
                // 尝试直接查询验证数据是否存在
                List<Merchant> allMerchants = merchantRepository.findAll();
                System.out.println("Service层: 数据库中总共有 " + allMerchants.size() + " 个商家");
                long scenicCount = allMerchants.stream()
                    .filter(m -> m.getScenicSpot() != null && m.getScenicSpot().getId().equals(scenicSpotId))
                    .count();
                System.out.println("Service层: 其中 scenic_id=" + scenicSpotId + " 的商家有 " + scenicCount + " 个");
            }
            return result;
        } catch (Exception e) {
            System.err.println("Service层: 查询时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 获取所有商家
     */
    @Transactional(readOnly = true)
    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAll().stream()
                .filter(m -> !Boolean.TRUE.equals(m.getDeleted()) && Boolean.TRUE.equals(m.getEnabled()))
                .collect(Collectors.toList());
    }
    
    /**
     * 删除商家
     */
    /**
     * 删除商家
     * 先删除关联的商家活动记录、订单、评论，再删除商家
     */
    public void deleteMerchant(Long id) {
        Optional<Merchant> merchantOpt = merchantRepository.findById(id);
        if (merchantOpt.isEmpty()) {
            throw new RuntimeException("商家不存在");
        }
        Merchant merchant = merchantOpt.get();

        // 1. 删除关联的订单（解决外键约束问题）
        if (orderService != null) {
            orderService.deleteOrdersByMerchantId(id);
            System.out.println("已删除商家 " + id + " 的关联订单");
        }

        // 2. 删除关联的评论（解决外键约束问题）
        if (reviewService != null) {
            reviewService.deleteReviewsByMerchantId(id);
            System.out.println("已删除商家 " + id + " 的关联评论");
        }

        // 3. 先删除关联的商家活动记录（解决外键约束问题）
        if (merchantActivityRepository != null) {
            int activityCount = merchantActivityRepository.deleteByMerchantId(id);
            if (activityCount > 0) {
                System.out.println("删除商家 " + id + " 的 " + activityCount + " 条活动记录");
            }
        }

        // 3.5 删除关联的房型（酒店商家）
        roomTypeRepository.deleteByMerchantId(id);
        System.out.println("已删除商家 " + id + " 的关联房型");

        // 3.6 删除商家上传的图片文件
        deleteUploadedFiles(merchant.getAvatar(), merchant.getShopImages());

        // 3.6 删除关联的团购订单和团购套餐（陶瓷工坊等）
        List<com.jdz.tourism.entity.GroupBuy> groupBuys = groupBuyRepository.findByMerchantIdOrderByCreatedAtDesc(id);
        for (com.jdz.tourism.entity.GroupBuy gb : groupBuys) {
            groupBuyOrderRepository.deleteAll(groupBuyOrderRepository.findByGroupBuyIdOrderByCreatedAtDesc(gb.getId()));
        }
        groupBuyRepository.deleteAll(groupBuys);
        System.out.println("已删除商家 " + id + " 的关联团购及订单");

        // 4. 强校验：子表必须先删净，否则不允许删主表
        long remainOrders = orderService != null ? orderRepository.countByMerchantId(id) : 0;
        boolean remainReviews = reviewService != null && reviewRepository.existsByMerchantId(id);
        boolean remainActivities = merchantActivityRepository != null && merchantActivityRepository.existsByMerchantId(id);
        if (remainOrders > 0 || remainReviews || remainActivities) {
            throw new RuntimeException("关联数据仍存在，已中止删除主表: orders="
                    + remainOrders + ", reviews=" + (remainReviews ? 1 : 0)
                    + ", activities=" + (remainActivities ? 1 : 0));
        }

        // 5. 景点商家执行物理删除，确保数据库中真实删除记录
        if ("SCENIC".equals(merchant.getCategory())) {
            merchantRepository.deleteById(id);
            System.out.println("已物理删除景点商家，ID: " + id);
            return;
        }

        // 6. 其他商家保留逻辑删除
        int deleted = merchantRepository.markDeletedById(id);
        if (deleted <= 0) {
            throw new RuntimeException("商家不存在或正在被其他事务占用");
        }
        System.out.println("已逻辑删除商家，ID: " + id);
    }
    
    /**
     * 删除用户关联的所有商家（包括逻辑删除的），用于删除用户时级联清理。
     * 会先清理商家所有子表数据，最后物理删除 merchant 记录，避免外键约束。
     */
    @Transactional
    public void deleteAllByUserId(Long userId) {
        // 查找该用户下所有商家（含逻辑删除的）
        merchantRepository.findByUserId(userId).ifPresent(merchant -> {
            Long id = merchant.getId();
            // 清理子表
            if (orderService != null) orderService.deleteOrdersByMerchantId(id);
            if (reviewService != null) reviewService.deleteReviewsByMerchantId(id);
            if (merchantActivityRepository != null) merchantActivityRepository.deleteByMerchantId(id);
            roomTypeRepository.deleteByMerchantId(id);
            List<com.jdz.tourism.entity.GroupBuy> groupBuys = groupBuyRepository.findByMerchantIdOrderByCreatedAtDesc(id);
            for (com.jdz.tourism.entity.GroupBuy gb : groupBuys) {
                groupBuyOrderRepository.deleteAll(groupBuyOrderRepository.findByGroupBuyIdOrderByCreatedAtDesc(gb.getId()));
            }
            groupBuyRepository.deleteAll(groupBuys);
            // 物理删除 merchant 记录，解除 user_id 外键约束
            merchantRepository.deleteById(id);
            System.out.println("已物理删除用户 " + userId + " 关联的商家，ID: " + id);
        });
    }

    /**
     * 根据分类获取商家
     */
    public List<Merchant> getMerchantsByCategory(String category) {
        return merchantRepository.findByCategoryAndDeletedFalseAndEnabledTrue(category);
    }
    
    /**
     * 计算商家的平均评分（基于真实评论数据）
     */
    public Double calculateMerchantRating(Long merchantId) {
        if (reviewService == null) {
            return 0.0; // 如果ReviewService不可用，返回0.0
        }
        try {
            Double rating = reviewService.getAverageRatingByMerchantId(merchantId);
            // 如果没有评论，返回0.0
            return rating != null ? rating : 0.0;
        } catch (Exception e) {
            // 如果出错，返回0.0
            return 0.0;
        }
    }
    
    /**
     * 获取商家的评论数量（基于真实评论数据）
     */
    public Long getMerchantReviewCount(Long merchantId) {
        if (reviewService == null) {
            return 0L; // 如果ReviewService不可用，返回0
        }
        try {
            return reviewService.getReviewCountByMerchantId(merchantId);
        } catch (Exception e) {
            return 0L;
        }
    }
    
    /**
     * 为商家对象添加评分和评论数量信息
     */
    public Map<String, Object> enrichMerchantWithRating(Merchant merchant) {
        Map<String, Object> merchantMap = new HashMap<>();
        merchantMap.put("id", merchant.getId());
        merchantMap.put("shopName", merchant.getShopName());
        merchantMap.put("description", merchant.getDescription());
        merchantMap.put("category", merchant.getCategory());
        merchantMap.put("address", merchant.getAddress());
        merchantMap.put("phone", merchant.getPhone());
        merchantMap.put("openTime", merchant.getOpenTime());
        merchantMap.put("startPrice", merchant.getStartPrice());
        merchantMap.put("avgPrice", merchant.getAvgPrice());
        merchantMap.put("cuisineType", merchant.getCuisineType());
        // 统一图片字段：cover + images（数组），并保留原字段兼容
        String cover = normalizeRelativeUrl(firstNonEmpty(merchant.getAvatar()));
        merchantMap.put("avatar", cover); // 兼容旧字段
        merchantMap.put("cover", cover);
        // 解析多图：支持逗号分隔或JSON数组字符串
        java.util.List<String> images = parseImages(merchant.getShopImages());
        if (images.isEmpty() && cover != null) {
            images.add(cover);
        }
        merchantMap.put("images", images);
        merchantMap.put("shopImages", String.join(",", images)); // 兼容旧前端
        
        // 评分逻辑：
        // 1. 对于景点类型商家（SCENIC），优先从关联的景点表获取评分
        // 2. 否则优先使用管理员评分，其次是用户评分
        Double rating = null;
        
        // 如果是景点类型商家，且有关联的景点，从景点表获取评分
        if ("SCENIC".equals(merchant.getCategory()) && merchant.getScenicSpot() != null) {
            ScenicSpot scenicSpot = merchant.getScenicSpot();
            // 如果景点有手动设置的评分，优先使用
            if (scenicSpot.getRating() != null && scenicSpot.getRating().compareTo(java.math.BigDecimal.ZERO) > 0) {
                rating = scenicSpot.getRating().doubleValue();
            } else if (reviewService != null) {
                // 否则从评论计算平均评分
                try {
                    Double avgRating = reviewService.getAverageRatingByScenicSpotId(scenicSpot.getId());
                    if (avgRating != null && avgRating > 0) {
                        rating = avgRating;
                    }
                } catch (Exception e) {
                    System.err.println("计算景点评分失败: " + e.getMessage());
                }
            }
        }
        
        // 如果不是景点类型，或景点没有评分，使用商家评分逻辑
        if (rating == null || rating == 0) {
            if (merchant.getAdminRating() != null && merchant.getAdminRating() > 0) {
                rating = merchant.getAdminRating();
            } else {
                rating = calculateMerchantRating(merchant.getId());
            }
        }
        
        // 评论数量：景点类型从景点评论获取，其他从商家评论获取
        Long reviewCount;
        if ("SCENIC".equals(merchant.getCategory()) && merchant.getScenicSpot() != null && reviewService != null) {
            try {
                reviewCount = reviewService.getReviewCountByScenicSpotId(merchant.getScenicSpot().getId());
            } catch (Exception e) {
                reviewCount = getMerchantReviewCount(merchant.getId());
            }
        } else {
            reviewCount = getMerchantReviewCount(merchant.getId());
        }
        
        merchantMap.put("rating", rating != null ? rating : 0.0);
        merchantMap.put("adminRating", merchant.getAdminRating()); // Optional: expose it explicitly if needed
        merchantMap.put("reviewCount", reviewCount != null ? reviewCount : 0L);
        
        // 标签（逗号分隔字符串 → 数组）
        merchantMap.put("tags", parseCommaSeparated(merchant.getTags()));
        // 设施（逗号分隔字符串 → 数组）
        merchantMap.put("facilities", parseCommaSeparated(merchant.getFacilities()));
        // 酒店专属字段
        merchantMap.put("starRating", merchant.getStarRating());
        merchantMap.put("roomCount", merchant.getRoomCount());
        // 启用状态（null 时默认 true）
        merchantMap.put("enabled", merchant.getEnabled() == null ? true : merchant.getEnabled());
        
        boolean qualificationApproved = false;
        if (merchant.getUser() != null && merchantApplicationRepository != null) {
            try {
                Long userId = merchant.getUser().getId();
                qualificationApproved = merchantApplicationRepository
                        .findByUserIdAndStatus(userId, MerchantApplication.ApplicationStatus.APPROVED)
                        .isPresent();
            } catch (Exception e) {
                qualificationApproved = false;
            }
        }
        merchantMap.put("qualificationApproved", qualificationApproved);

        if (merchant.getScenicSpot() != null && merchant.getScenicSpot().getId() != null) {
            merchantMap.put("scenicSpotId", merchant.getScenicSpot().getId());
        } else {
            merchantMap.put("scenicSpotId", null);
        }
        
        if (merchant.getUser() != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", merchant.getUser().getId());
            userMap.put("username", merchant.getUser().getUsername());
            merchantMap.put("user", userMap);
        }

        // 添加进行中的活动
        if (merchantActivityRepository != null) {
            LocalDateTime now = LocalDateTime.now();
            List<MerchantActivity> activities = merchantActivityRepository.findByMerchantIdOrderByStartTimeDesc(merchant.getId());
            List<Map<String, Object>> activeActivities = activities.stream()
                .filter(a -> {
                    // 保留未结束的活动（进行中 + 未开始）
                    return now.isBefore(a.getEndTime());
                })
                .map(a -> {
                    Map<String, Object> act = new HashMap<>();
                    act.put("id", a.getId());
                    act.put("title", a.getTitle());
                    act.put("description", a.getDescription());
                    act.put("type", a.getType());
                    act.put("startTime", a.getStartTime());
                    act.put("endTime", a.getEndTime());
                    return act;
                })
                .collect(Collectors.toList());
            merchantMap.put("activities", activeActivities);
        }
        
        return merchantMap;
    }

    private static java.util.List<String> parseCommaSeparated(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new java.util.ArrayList<>();
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String s : raw.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    private static String firstNonEmpty(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }

    private static String normalizeRelativeUrl(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        String u = url.trim();
        if (u.startsWith("http") || u.startsWith("data:image/")) return u;
        if (u.startsWith("/")) return u;
        return "/" + u;
    }

    private static java.util.List<String> parseImages(String raw) {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        if (raw == null || raw.trim().isEmpty()) return new java.util.ArrayList<>(set);
        String s = raw.trim();
        try {
            if ((s.startsWith("[") && s.endsWith("]"))) {
                // JSON数组
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<String> arr = mapper.readValue(s, mapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                for (String it : arr) {
                    if (it != null && !it.trim().isEmpty()) {
                        set.add(normalizeRelativeUrl(it));
                    }
                }
            } else {
                for (String it : s.split(",")) {
                    if (it != null && !it.trim().isEmpty()) {
                        set.add(normalizeRelativeUrl(it));
                    }
                }
            }
        } catch (Exception ignore) {
            for (String it : s.split(",")) {
                if (it != null && !it.trim().isEmpty()) {
                    set.add(normalizeRelativeUrl(it));
                }
            }
        }
        return new java.util.ArrayList<>(set);
    }

    // ==================== 智能推荐算法 ====================

    /**
     * 按分类推荐商家（个性化）
     * 算法与景点推荐一致：Jaccard 标签匹配 + 热度，冷启动降级为评分排序
     *
     * @param category 商家分类（FOOD / HOTEL / CERAMIC 等）
     * @param userId   当前用户ID，null 时走冷启动
     */
    public List<Map<String, Object>> recommendMerchants(String category, Long userId) {
        List<Merchant> all = getMerchantsByCategory(category);
        List<Map<String, Object>> enriched = all.stream()
                .map(this::enrichMerchantWithRating)
                .collect(Collectors.toList());

        if (enriched.isEmpty()) return enriched;

        // 读取算法配置
        String algorithm = "hybrid";
        if (systemConfigService != null) {
            String cfg = systemConfigService.getConfigValue("system.recommendAlgorithm");
            if (cfg != null && !cfg.isBlank()) algorithm = cfg.trim();
        }

        // 构建用户画像（商家收藏 + 商家评论标签）
        Map<String, Integer> userProfile = buildMerchantUserProfile(userId);

        // 冷启动：无行为数据时按评分降序
        if (userProfile.isEmpty()) {
            enriched.sort((a, b) -> {
                double ra = toDouble(a.get("rating"));
                double rb = toDouble(b.get("rating"));
                return Double.compare(rb, ra);
            });
            return enriched;
        }

        // 热度归一化基准（用 reviewCount 代替 visitCount）
        double maxReview = enriched.stream()
                .mapToDouble(m -> toDouble(m.get("reviewCount")))
                .max().orElse(1.0);
        if (maxReview == 0) maxReview = 1.0;

        // 已交互商家 ID（降权）
        Set<Long> interactedIds = getInteractedMerchantIds(userId);

        final double simW;
        final double popW;
        if ("content".equals(algorithm)) {
            simW = 1.0; popW = 0.0;
        } else if ("collaborative".equals(algorithm)) {
            return collaborativeMerchantFilter(userId, enriched, interactedIds, category);
        } else {
            simW = 0.7; popW = 0.3;
        }

        final double maxR = maxReview;
        enriched.sort((a, b) -> {
            double simA = jaccardMerchant(userProfile, parseMerchantTags(a));
            double popA = toDouble(a.get("reviewCount")) / maxR;
            double scoreA = simA * simW + popA * popW;
            if (interactedIds.contains(toLong(a.get("id")))) scoreA *= 0.5;

            double simB = jaccardMerchant(userProfile, parseMerchantTags(b));
            double popB = toDouble(b.get("reviewCount")) / maxR;
            double scoreB = simB * simW + popB * popW;
            if (interactedIds.contains(toLong(b.get("id")))) scoreB *= 0.5;

            return Double.compare(scoreB, scoreA);
        });
        return enriched;
    }

    /** 构建用户对商家的兴趣画像（商家收藏 + 商家评论） */
    private Map<String, Integer> buildMerchantUserProfile(Long userId) {
        Map<String, Integer> profile = new HashMap<>();
        if (userId == null) return profile;

        // 商家收藏标签
        try {
            List<com.jdz.tourism.entity.MerchantFavorite> favs = merchantFavoriteRepository.findByUserId(userId);
            for (com.jdz.tourism.entity.MerchantFavorite fav : favs) {
                Merchant m = fav.getMerchant();
                if (m == null) continue;
                for (String tag : splitTags(m.getTags())) {
                    profile.merge(tag, 1, Integer::sum);
                }
                // category 也作为标签
                if (m.getCategory() != null) profile.merge(m.getCategory(), 1, Integer::sum);
            }
        } catch (Exception ignored) {}

        // 商家评论标签（高分权重 x2）
        if (reviewService != null) {
            try {
                List<com.jdz.tourism.entity.Review> reviews = reviewService.getReviewsByUserId(userId);
                for (com.jdz.tourism.entity.Review r : reviews) {
                    if (r.getMerchant() == null) continue;
                    Merchant m = r.getMerchant();
                    int weight = (r.getRating() != null && r.getRating() >= 4) ? 2 : 1;
                    for (String tag : splitTags(m.getTags())) {
                        profile.merge(tag, weight, Integer::sum);
                    }
                    if (m.getCategory() != null) profile.merge(m.getCategory(), weight, Integer::sum);
                }
            } catch (Exception ignored) {}
        }
        return profile;
    }

    /** 获取用户已收藏 + 已评论的商家 ID（用于降权） */
    private Set<Long> getInteractedMerchantIds(Long userId) {
        Set<Long> ids = new HashSet<>();
        if (userId == null) return ids;
        try {
            merchantFavoriteRepository.findByUserId(userId)
                    .forEach(f -> { if (f.getMerchant() != null) ids.add(f.getMerchant().getId()); });
        } catch (Exception ignored) {}
        if (reviewService != null) {
            try {
                reviewService.getReviewsByUserId(userId).stream()
                        .filter(r -> r.getMerchant() != null)
                        .forEach(r -> ids.add(r.getMerchant().getId()));
            } catch (Exception ignored) {}
        }
        return ids;
    }

    /** 商家协同过滤 */
    private List<Map<String, Object>> collaborativeMerchantFilter(
            Long userId, List<Map<String, Object>> all,
            Set<Long> interactedIds, String category) {
        Map<Long, Set<Long>> userMatrix = new HashMap<>();
        try {
            List<Object[]> pairs = merchantFavoriteRepository.findAllUserMerchantPairs();
            for (Object[] row : pairs) {
                Long uid = ((Number) row[0]).longValue();
                Long mid = ((Number) row[1]).longValue();
                userMatrix.computeIfAbsent(uid, k -> new HashSet<>()).add(mid);
            }
        } catch (Exception e) {
            return fallbackMerchantSort(userId, all, interactedIds);
        }

        Set<Long> myFavs = userMatrix.getOrDefault(userId, java.util.Collections.emptySet());
        if (myFavs.isEmpty()) return fallbackMerchantSort(userId, all, interactedIds);

        // Top-10 邻居
        List<Map.Entry<Long, Double>> neighbors = userMatrix.entrySet().stream()
                .filter(e -> !e.getKey().equals(userId))
                .map(e -> {
                    Set<Long> other = e.getValue();
                    long inter = myFavs.stream().filter(other::contains).count();
                    long union = myFavs.size() + other.size() - inter;
                    double j = union == 0 ? 0.0 : (double) inter / union;
                    return new java.util.AbstractMap.SimpleEntry<>(e.getKey(), j);
                })
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10)
                .collect(Collectors.toList());

        Map<Long, Double> scores = new HashMap<>();
        for (Map.Entry<Long, Double> nb : neighbors) {
            double sim = nb.getValue();
            for (Long mid : userMatrix.get(nb.getKey())) {
                if (!myFavs.contains(mid)) scores.merge(mid, sim, Double::sum);
            }
        }

        Map<Long, Map<String, Object>> byId = all.stream()
                .collect(Collectors.toMap(m -> toLong(m.get("id")), m -> m, (a, b) -> a));

        List<Map<String, Object>> result = scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(e -> byId.get(e.getKey()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (result.size() < 5) {
            Set<Long> seen = result.stream().map(m -> toLong(m.get("id"))).collect(Collectors.toSet());
            fallbackMerchantSort(userId, all, interactedIds).stream()
                    .filter(m -> !seen.contains(toLong(m.get("id"))))
                    .forEach(result::add);
        }
        return result;
    }

    private List<Map<String, Object>> fallbackMerchantSort(Long userId,
            List<Map<String, Object>> all, Set<Long> interactedIds) {
        Map<String, Integer> profile = buildMerchantUserProfile(userId);
        double maxR = all.stream().mapToDouble(m -> toDouble(m.get("reviewCount"))).max().orElse(1.0);
        if (maxR == 0) maxR = 1.0;
        final double mr = maxR;
        all.sort((a, b) -> {
            double sa = jaccardMerchant(profile, parseMerchantTags(a)) * 0.7
                    + toDouble(a.get("reviewCount")) / mr * 0.3;
            if (interactedIds.contains(toLong(a.get("id")))) sa *= 0.5;
            double sb = jaccardMerchant(profile, parseMerchantTags(b)) * 0.7
                    + toDouble(b.get("reviewCount")) / mr * 0.3;
            if (interactedIds.contains(toLong(b.get("id")))) sb *= 0.5;
            return Double.compare(sb, sa);
        });
        return all;
    }

    private double jaccardMerchant(Map<String, Integer> profile, Set<String> tags) {
        if (profile.isEmpty() || tags.isEmpty()) return 0.0;
        double inter = 0.0;
        for (String t : tags) if (profile.containsKey(t)) inter += profile.get(t);
        double union = profile.values().stream().mapToInt(Integer::intValue).sum() + tags.size() - inter;
        return union == 0 ? 0.0 : inter / union;
    }

    private Set<String> parseMerchantTags(Map<String, Object> m) {
        Set<String> tags = new HashSet<>();
        Object tagsObj = m.get("tags");
        if (tagsObj instanceof List<?>) {
            for (Object t : (List<?>) tagsObj) if (t != null) tags.add(t.toString().trim());
        } else if (tagsObj instanceof String) {
            for (String t : ((String) tagsObj).split("[,，\\s]+")) {
                String s = t.trim(); if (!s.isEmpty()) tags.add(s);
            }
        }
        Object cat = m.get("category");
        if (cat != null && !cat.toString().isBlank()) tags.add(cat.toString().trim());
        return tags;
    }

    private static Set<String> splitTags(String raw) {
        Set<String> set = new HashSet<>();
        if (raw == null || raw.isBlank()) return set;
        for (String t : raw.split("[,，\\s]+")) { String s = t.trim(); if (!s.isEmpty()) set.add(s); }
        return set;
    }

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0.0; }
    }

    private static Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }

    /**
     * 为没有关联账号的商家自动生成账号
     */
    @Transactional
    public Map<String, Object> syncMerchantUsers() {
        List<Merchant> allMerchants = merchantRepository.findAll();
        int createdCount = 0;
        int totalCount = allMerchants.size();
        
        for (Merchant merchant : allMerchants) {
            // 1. 同步账号（对所有类型的商家生效，包括餐饮、酒店、陶瓷等）
            if (merchant.getUser() == null) {
                try {
                    // 创建新用户
                    User user = new User();
                    String username = "shop_" + merchant.getId();
                    
                    // 如果用户名已存在，尝试加后缀
                    int suffix = 1;
                    while (userRepository.existsByUsername(username)) {
                        username = "shop_" + merchant.getId() + "_" + suffix++;
                    }
                    
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setRole(User.UserRole.MERCHANT);
                    
                    User savedUser = userRepository.save(user);
                    
                    merchant.setUser(savedUser);
                    createdCount++;
                } catch (Exception e) {
                    System.err.println("为商家 " + merchant.getId() + " 创建账号失败: " + e.getMessage());
                }
            }
            
            // 2. 如果是景点类型的商家，同步景点数据到商家表
            if ("SCENIC".equals(merchant.getCategory()) && merchant.getScenicSpot() != null) {
                ScenicSpot spot = merchant.getScenicSpot();
                boolean updated = false;
                
                // 同步图片
                if (merchant.getAvatar() == null && spot.getImage() != null) {
                    merchant.setAvatar(spot.getImage());
                    updated = true;
                }
                if (merchant.getShopImages() == null && spot.getScenicImages() != null) {
                    merchant.setShopImages(spot.getScenicImages());
                    updated = true;
                }
                
                // 同步描述
                if ((merchant.getDescription() == null || merchant.getDescription().isEmpty()) 
                        && spot.getDescription() != null) {
                    merchant.setDescription(spot.getDescription());
                    updated = true;
                }
                
                // 同步价格
                if (merchant.getStartPrice() == null && spot.getPrice() != null) {
                    merchant.setStartPrice(spot.getPrice());
                    updated = true;
                }
                
                // 同步营业时间
                if (merchant.getOpenTime() == null && spot.getOpenTime() != null) {
                    merchant.setOpenTime(spot.getOpenTime());
                    updated = true;
                }
                
                if (updated) {
                    System.out.println("同步景点数据到商家: " + merchant.getShopName());
                }
            }
            
            merchantRepository.save(merchant);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", totalCount);
        result.put("created", createdCount);
        return result;
    }
}