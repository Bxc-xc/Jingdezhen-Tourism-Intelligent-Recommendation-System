package com.jdz.tourism.service;

import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.entity.Favorite;
import com.jdz.tourism.repository.ScenicSpotRepository;
import com.jdz.tourism.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Random;
import java.util.AbstractMap;
import java.util.stream.Collectors;

@Service
public class ScenicSpotService {
    
    @Autowired
    private ScenicSpotRepository scenicSpotRepository;

    @Autowired
    private MerchantService merchantService;

    @Autowired(required = false)
    private ReviewService reviewService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired(required = false)
    private SystemConfigService systemConfigService;

    
    /**
     * 获取所有景点
     * 统一通过MerchantService按SCENIC分类查询（与餐饮、酒店、陶瓷工坊逻辑一致）
     */
    public List<ScenicSpot> getAllScenicSpots() {
        // 通过MerchantService获取SCENIC分类的商家（景点）
        List<Merchant> merchants = merchantService.getMerchantsByCategory("SCENIC");
        List<ScenicSpot> result = new ArrayList<>();
        
        for (Merchant m : merchants) {
            ScenicSpot spot = convertMerchantToScenicSpot(m);
            if (spot != null) {
                // 计算评分（如果未手动设置）
                calculateRatingIfNeeded(spot);
                
                // 确保填充商家关联的用户信息
                if (m.getUser() != null) {
                    spot.setUser(m.getUser());
                }
                
                result.add(spot);
            }
        }
        
        // 同时包含系统景点表中未关联商家的景点（向后兼容）
        List<ScenicSpot> systemSpots = scenicSpotRepository.findAll();
        for (ScenicSpot spot : systemSpots) {
            // 检查是否已经在商家列表中（通过ID判断）
            boolean exists = result.stream()
                    .anyMatch(s -> s.getId().equals(spot.getId()));
            if (!exists) {
                // 计算评分（如果未手动设置）
                calculateRatingIfNeeded(spot);
                
                // 尝试查找关联的商家并设置User
                try {
                    Optional<Merchant> merchantOpt = merchantService.getMerchantByScenicSpotId(spot.getId());
                    if (merchantOpt.isPresent()) {
                        Merchant m = merchantOpt.get();
                        if (m.getUser() != null) {
                            spot.setUser(m.getUser());
                        }
                    }
                } catch (Exception e) {
                    // 忽略错误，不影响列表显示
                }
                
                result.add(withDefaultImage(spot));
            }
        }
        
        return result;
    }

    /**
     * 景点分页（SQL 分页）：用于列表分页场景。
     * 注意：这里分页基于 scenic_spot 表（Repository），不包含 MerchantService 合并逻辑，
     * 以确保 offset/limit 由数据库执行且结果稳定可分页。
     */
    public Page<ScenicSpot> getScenicSpotsPage(int page, int size, String keyword, String sortField, String sortOrder) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 200);

        // 构建排序：支持 price / rating / visitCount，默认按 id 升序
        Sort sort;
        if (sortField != null && !sortField.isBlank()) {
            String col = switch (sortField) {
                case "price"         -> "price";
                case "rating"        -> "rating";
                case "favoriteCount" -> "visitCount";
                default              -> "id";
            };
            sort = "asc".equalsIgnoreCase(sortOrder)
                    ? Sort.by(col).ascending()
                    : Sort.by(col).descending();
        } else {
            sort = Sort.by("id").ascending();
        }
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, sort);

        Page<ScenicSpot> result;
        if (keyword != null && !keyword.isBlank()) {
            result = scenicSpotRepository.findByKeywordInNameOrDescriptionOrTags(keyword.trim(), pageable);
        } else {
            result = scenicSpotRepository.findAll(pageable);
        }

        // 填充每个景点关联的商家账号（user 字段为 @Transient，需手动填充）
        result.getContent().forEach(spot -> {
            try {
                Optional<Merchant> merchantOpt = merchantService.getMerchantByScenicSpotId(spot.getId());
                if (merchantOpt.isPresent() && merchantOpt.get().getUser() != null) {
                    spot.setUser(merchantOpt.get().getUser());
                }
            } catch (Exception e) {
                // 忽略单条查询失败，不影响整体分页
            }
        });

        return result;
    }
    
    /**
     * 根据ID获取景点详情
     * 统一通过MerchantService查询SCENIC分类的商家（与餐饮、酒店、陶瓷工坊逻辑一致）
     */
    public Optional<ScenicSpot> getScenicSpotById(Long id) {
        System.out.println("========== Service层: 查询景点ID = " + id + " ==========");
        
        // 1. 优先通过MerchantService查询（统一逻辑）
        Optional<Merchant> merchantOpt = merchantService.getMerchantById(id);
        System.out.println("通过商家ID查询结果: " + (merchantOpt.isPresent() ? "找到商家" : "未找到"));
        
        if (!merchantOpt.isPresent()) {
            System.out.println("尝试通过scenic_id查询: " + id);
            merchantOpt = merchantService.getMerchantByScenicSpotId(id);
            System.out.println("通过scenic_id查询结果: " + (merchantOpt.isPresent() ? "找到商家" : "未找到"));
        }
        
        if (merchantOpt.isPresent()) {
            Merchant m = merchantOpt.get();
            System.out.println("找到商家: ID=" + m.getId() + ", shopName=" + m.getShopName() + ", category=" + m.getCategory());
            
            // 只处理SCENIC分类的商家
            if ("SCENIC".equals(m.getCategory())) {
                System.out.println("商家分类为SCENIC，开始转换");
                ScenicSpot spot = convertMerchantToScenicSpot(m);
                if (spot != null) {
                    System.out.println("成功转换为景点: " + spot.getName());
                    // 如果关联了系统景点，增加访问量
                    if (m.getScenicSpot() != null && m.getScenicSpot().getId() != null) {
                        ScenicSpot systemSpot = m.getScenicSpot();
                        systemSpot.setVisitCount(systemSpot.getVisitCount() + 1);
                        scenicSpotRepository.save(systemSpot);
                        spot.setVisitCount(systemSpot.getVisitCount());
                        if (systemSpot.getRating() != null && systemSpot.getRating().compareTo(java.math.BigDecimal.ZERO) > 0) {
                            spot.setRating(systemSpot.getRating());
                        }
                    }
                    calculateRatingIfNeeded(spot);
                    try { fillCommentCount(spot); } catch (Exception ignore) {}
                    return Optional.of(withDefaultImage(spot));
                } else {
                    System.out.println("convertMerchantToScenicSpot返回null");
                }
            } else {
                System.out.println("商家分类不是SCENIC，当前分类: " + m.getCategory());
            }
        }
        
        // 2. 向后兼容：尝试从系统景点表查询
        System.out.println("尝试从系统景点表查询: " + id);
        Optional<ScenicSpot> scenicSpot = scenicSpotRepository.findById(id);
        if (scenicSpot.isPresent()) {
            System.out.println("从系统景点表找到景点: " + scenicSpot.get().getName());
            ScenicSpot spot = scenicSpot.get();
            spot.setVisitCount(spot.getVisitCount() + 1);
            scenicSpotRepository.save(spot);
            // 计算评分（如果未手动设置）
            calculateRatingIfNeeded(spot);
            try { fillCommentCount(spot); } catch (Exception ignore) {}
            return Optional.of(withDefaultImage(spot));
        }
        
        System.out.println("未找到景点，ID = " + id);
        return Optional.empty();
    }
    
    /**
     * 根据分类获取景点
     * 统一通过MerchantService按分类查询（与餐饮、酒店、陶瓷工坊逻辑一致）
     */
    public List<ScenicSpot> getScenicSpotsByCategory(String category) {
        // 如果是SCENIC分类，通过MerchantService查询
        if ("SCENIC".equalsIgnoreCase(category)) {
            List<Merchant> merchants = merchantService.getMerchantsByCategory("SCENIC");
            return merchants.stream()
                    .map(this::convertMerchantToScenicSpot)
                    .filter(spot -> spot != null)
                    .peek(this::calculateRatingIfNeeded)
                    .peek(s -> { try { fillCommentCount(s); } catch (Exception ignore) {} })
                    .map(this::withDefaultImage)
                    .collect(Collectors.toList());
        }
        
        // 其他分类从系统景点表查询（向后兼容）
        return scenicSpotRepository.findByCategory(category)
                .stream()
                .peek(this::calculateRatingIfNeeded)
                .peek(s -> { try { fillCommentCount(s); } catch (Exception ignore) {} })
                .map(this::withDefaultImage)
                .toList();
    }
    
    /**
     * 根据标签获取景点
     */
    public List<ScenicSpot> getScenicSpotsByTag(String tag) {
        return scenicSpotRepository.findByTagsContaining(tag)
                .stream()
                .map(this::withDefaultImage)
                .toList();
    }
    
    /**
     * 根据多个标签获取景点（匹配任意一个标签）
     */
    public List<ScenicSpot> getScenicSpotsByTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        
        // 使用Set去重，因为一个景点可能匹配多个标签
        Set<ScenicSpot> resultSet = new HashSet<>();
        for (String tag : tags) {
            List<ScenicSpot> spots = scenicSpotRepository.findByTagsIn(tag);
            resultSet.addAll(spots);
        }
        
        return new ArrayList<>(resultSet).stream()
                .map(this::withDefaultImage)
                .toList();
    }
    
    /**
     * 获取热门景点（按访问量排序，去重）
     * 统一通过MerchantService查询SCENIC分类的商家
     */
    public List<ScenicSpot> getPopularScenicSpots() {
        // 从系统景点表获取热门景点（按访问量）
        List<ScenicSpot> systemPopular = scenicSpotRepository.findTop10ByOrderByVisitCountDesc();
        
        // 从MerchantService获取SCENIC分类的商家
        List<Merchant> merchants = merchantService.getMerchantsByCategory("SCENIC");
        List<ScenicSpot> merchantSpots = merchants.stream()
                .map(this::convertMerchantToScenicSpot)
                .filter(spot -> spot != null)
                .collect(Collectors.toList());
        
        // 合并并去重，保留访问量最高的
        // 同时确保评分一致性：如果系统景点和商家景点都存在，优先使用系统景点表中的评分
        Map<String, ScenicSpot> merged = new HashMap<>();
        Map<Long, ScenicSpot> systemSpotsById = new HashMap<>();
        systemPopular.forEach(spot -> {
            calculateRatingIfNeeded(spot);
            try { fillCommentCount(spot); } catch (Exception ignore) {}
            String key = spot.getName();
            systemSpotsById.put(spot.getId(), spot);
            if (!merged.containsKey(key) || merged.get(key).getVisitCount() < spot.getVisitCount()) {
                merged.put(key, spot);
            }
        });
        merchantSpots.forEach(spot -> {
            calculateRatingIfNeeded(spot);
            try { fillCommentCount(spot); } catch (Exception ignore) {}
            String key = spot.getName();
            // 如果系统景点表中也有这个景点，确保使用系统景点表中的评分（管理员手动设置的）
            ScenicSpot systemSpot = systemSpotsById.get(spot.getId());
            if (systemSpot != null && systemSpot.getRating() != null && 
                systemSpot.getRating().compareTo(java.math.BigDecimal.ZERO) > 0) {
                spot.setRating(systemSpot.getRating());
            }
            if (!merged.containsKey(key) || 
                (merged.get(key).getVisitCount() != null && spot.getVisitCount() != null &&
                 merged.get(key).getVisitCount() < spot.getVisitCount())) {
                merged.put(key, spot);
            }
        });
        
        return merged.values().stream()
                .map(this::withDefaultImage)
                .sorted((a, b) -> Long.compare(
                    b.getVisitCount() != null ? b.getVisitCount() : 0L,
                    a.getVisitCount() != null ? a.getVisitCount() : 0L))
                .limit(8)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据名称搜索景点（优化版：综合搜索名称、描述、标签）
     */
    public List<ScenicSpot> searchScenicSpotsByName(String name) {
        // 使用综合搜索，提高搜索准确性
        List<ScenicSpot> results = scenicSpotRepository.findByKeywordInNameOrDescriptionOrTags(name);
        
        // 如果综合搜索没有结果，尝试单独的名称搜索
        if (results.isEmpty()) {
            results = scenicSpotRepository.findByNameContainingIgnoreCase(name);
        }
        
        return results.stream()
                .peek(this::calculateRatingIfNeeded)
                .peek(s -> { try { fillCommentCount(s); } catch (Exception ignore) {} })
                .map(this::withDefaultImage)
                .toList();
    }
    
    /**
     * 根据名称精确获取景点（用于去重检查）
     */
    public Optional<ScenicSpot> getScenicSpotByName(String name) {
        // 1. 优先通过MerchantService查询
        Optional<Merchant> merchantOpt = merchantService.getMerchantByShopName(name);
        if (merchantOpt.isPresent() && "SCENIC".equals(merchantOpt.get().getCategory())) {
            ScenicSpot spot = convertMerchantToScenicSpot(merchantOpt.get());
            calculateRatingIfNeeded(spot);
            return Optional.ofNullable(spot);
        }
        
        // 2. 从系统景点表查询
        return scenicSpotRepository.findByName(name)
                .map(this::withDefaultImage);
    }

    /**
     * 根据价格范围搜索景点
     */
    public List<ScenicSpot> searchScenicSpotsByPriceRange(Double minPrice, Double maxPrice) {
        java.math.BigDecimal min = minPrice == null ? java.math.BigDecimal.ZERO : java.math.BigDecimal.valueOf(minPrice);
        java.math.BigDecimal max = maxPrice == null ? new java.math.BigDecimal("9999999") : java.math.BigDecimal.valueOf(maxPrice);
        if (min.compareTo(max) > 0) {
            java.math.BigDecimal tmp = min; min = max; max = tmp;
        }
        return scenicSpotRepository.findByPriceBetween(min, max)
                .stream()
                .peek(this::calculateRatingIfNeeded)
                .peek(s -> { try { fillCommentCount(s); } catch (Exception ignore) {} })
                .map(this::withDefaultImage)
                .toList();
    }
    
    /**
     * 推荐景点：根据管理员配置的算法策略执行推荐
     *
     * 算法策略（管理员端系统设置可切换）：
     * - content（内容推荐）：基于用户收藏/评分行为构建用户画像，Jaccard 相似度匹配景点标签
     * - hybrid（混合推荐，默认）：内容推荐 × 0.7 + 热度 × 0.3
     * - collaborative（协同过滤）：当前降级为内容推荐（数据量不足时的兜底）
     * - 冷启动：新用户无行为数据时统一降级为热度排序
     */
    public List<ScenicSpot> recommendScenicSpots(Long userId) {
        // 1. 获取全量景点并去重
        List<ScenicSpot> allSpots = getAllScenicSpotsIncludingMerchants();
        Set<Long> seenIds = new HashSet<>();
        allSpots = allSpots.stream()
                .filter(spot -> seenIds.add(spot.getId()))
                .map(this::withDefaultImage)
                .collect(Collectors.toList());

        if (allSpots.isEmpty()) return allSpots;

        // 2. 读取管理员配置的推荐算法（默认 hybrid）
        String algorithm = "hybrid";
        if (systemConfigService != null) {
            String cfg = systemConfigService.getConfigValue("system.recommendAlgorithm");
            if (cfg != null && !cfg.isBlank()) algorithm = cfg.trim();
        }

        // 3. 构建用户画像
        Map<String, Integer> userProfile = buildUserProfile(userId);

        // 4. 冷启动：无行为数据时统一热度排序
        if (userProfile.isEmpty()) {
            return allSpots.stream()
                    .sorted(Comparator.comparingLong(
                            s -> -(s.getVisitCount() != null ? s.getVisitCount() : 0L)))
                    .collect(Collectors.toList());
        }

        // 5. 计算热度归一化基准
        long maxVisit = allSpots.stream()
                .mapToLong(s -> s.getVisitCount() != null ? s.getVisitCount() : 0L)
                .max().orElse(1L);

        // 6. 已交互景点 ID（用于降权）
        Set<Long> interactedIds = getInteractedScenicIds(userId);

        // 7. 根据算法策略计算权重
        final double similarityWeight;
        final double popularityWeight;
        switch (algorithm) {
            case "content":
                // 纯内容推荐：完全依赖标签相似度
                similarityWeight = 1.0;
                popularityWeight = 0.0;
                break;
            case "collaborative":
                // 真正的协同过滤：基于用户行为相似度推荐
                return collaborativeFilter(userId, allSpots, interactedIds);
            default:
                // hybrid（混合推荐）：相似度 0.7 + 热度 0.3
                similarityWeight = 0.7;
                popularityWeight = 0.3;
        }

        // 8. 打分排序
        return allSpots.stream()
                .map(spot -> {
                    double sim = jaccardSimilarity(userProfile, parseTags(spot));
                    double pop = (double)(spot.getVisitCount() != null ? spot.getVisitCount() : 0L) / maxVisit;
                    double score = sim * similarityWeight + pop * popularityWeight;
                    if (interactedIds.contains(spot.getId())) score *= 0.5;
                    return new AbstractMap.SimpleEntry<>(spot, score);
                })
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 基于用户的协同过滤（User-Based Collaborative Filtering）
     *
     * 算法步骤：
     * 1. 加载所有用户的收藏记录，构建 userId → Set<scenicId> 的行为矩阵
     * 2. 用 Jaccard 相似度计算当前用户与其他用户的相似度
     *    Jaccard(A,B) = |A∩B| / |A∪B|
     * 3. 取相似度最高的 Top-K 邻居用户（K=10）
     * 4. 统计邻居用户收藏过但当前用户未收藏的景点，
     *    按"邻居相似度加权的收藏频次"排序
     * 5. 协同过滤结果不足时，用内容推荐补齐（冷启动兜底）
     */
    private List<ScenicSpot> collaborativeFilter(Long userId,
                                                  List<ScenicSpot> allSpots,
                                                  Set<Long> interactedIds) {
        // Step 1: 构建全量行为矩阵 userId → Set<scenicId>
        Map<Long, Set<Long>> userMatrix = new HashMap<>();
        try {
            List<Object[]> pairs = favoriteRepository.findAllUserScenicPairs();
            for (Object[] row : pairs) {
                Long uid = ((Number) row[0]).longValue();
                Long sid = ((Number) row[1]).longValue();
                userMatrix.computeIfAbsent(uid, k -> new HashSet<>()).add(sid);
            }
        } catch (Exception e) {
            // 查询失败时降级为内容推荐
            return contentBasedFallback(userId, allSpots, interactedIds);
        }

        Set<Long> currentUserFavs = userMatrix.getOrDefault(userId, Collections.emptySet());

        // 当前用户无收藏记录 → 冷启动降级
        if (currentUserFavs.isEmpty()) {
            return contentBasedFallback(userId, allSpots, interactedIds);
        }

        // Step 2 & 3: 计算与其他用户的 Jaccard 相似度，取 Top-10 邻居
        final int TOP_K = 10;
        List<Map.Entry<Long, Double>> neighbors = userMatrix.entrySet().stream()
                .filter(e -> !e.getKey().equals(userId))
                .map(e -> {
                    Set<Long> other = e.getValue();
                    // Jaccard = |交集| / |并集|
                    long intersection = currentUserFavs.stream().filter(other::contains).count();
                    long union = currentUserFavs.size() + other.size() - intersection;
                    double jaccard = union == 0 ? 0.0 : (double) intersection / union;
                    return new AbstractMap.SimpleEntry<>(e.getKey(), jaccard);
                })
                .filter(e -> e.getValue() > 0)  // 只保留有交集的用户
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(TOP_K)
                .collect(Collectors.toList());

        // Step 4: 统计邻居收藏过但当前用户未收藏的景点，按加权频次排序
        Map<Long, Double> scenicScore = new HashMap<>();
        for (Map.Entry<Long, Double> neighbor : neighbors) {
            double sim = neighbor.getValue();
            Set<Long> neighborFavs = userMatrix.get(neighbor.getKey());
            for (Long sid : neighborFavs) {
                if (!currentUserFavs.contains(sid)) {
                    // 每个邻居贡献的分数 = 该邻居与当前用户的相似度
                    scenicScore.merge(sid, sim, Double::sum);
                }
            }
        }

        // Step 5: 按协同过滤分数排序，构建推荐列表
        Map<Long, ScenicSpot> spotMap = allSpots.stream()
                .collect(Collectors.toMap(ScenicSpot::getId, s -> s, (a, b) -> a));

        List<ScenicSpot> cfResult = scenicScore.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(e -> spotMap.get(e.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Step 6: 协同过滤结果不足 5 条时，用内容推荐补齐
        if (cfResult.size() < 5) {
            List<ScenicSpot> fallback = contentBasedFallback(userId, allSpots, interactedIds);
            Set<Long> cfIds = cfResult.stream().map(ScenicSpot::getId).collect(Collectors.toSet());
            for (ScenicSpot s : fallback) {
                if (!cfIds.contains(s.getId())) cfResult.add(s);
            }
        }

        return cfResult;
    }

    /**
     * 内容推荐降级方法（协同过滤数据不足时调用）
     * 相似度 0.7 + 热度 0.3
     */
    private List<ScenicSpot> contentBasedFallback(Long userId,
                                                    List<ScenicSpot> allSpots,
                                                    Set<Long> interactedIds) {
        Map<String, Integer> userProfile = buildUserProfile(userId);
        long maxVisit = allSpots.stream()
                .mapToLong(s -> s.getVisitCount() != null ? s.getVisitCount() : 0L)
                .max().orElse(1L);

        if (userProfile.isEmpty()) {
            return allSpots.stream()
                    .sorted(Comparator.comparingLong(
                            s -> -(s.getVisitCount() != null ? s.getVisitCount() : 0L)))
                    .collect(Collectors.toList());
        }

        return allSpots.stream()
                .map(spot -> {
                    double sim = jaccardSimilarity(userProfile, parseTags(spot));
                    double pop = (double)(spot.getVisitCount() != null ? spot.getVisitCount() : 0L) / maxVisit;
                    double score = sim * 0.7 + pop * 0.3;
                    if (interactedIds.contains(spot.getId())) score *= 0.5;
                    return new AbstractMap.SimpleEntry<>(spot, score);
                })
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 构建用户画像：统计用户收藏和评分景点的标签频次
     * 评分 >= 4 的景点标签权重 x2（高评价偏好强化）
     */
    private Map<String, Integer> buildUserProfile(Long userId) {
        Map<String, Integer> profile = new HashMap<>();
        if (userId == null) return profile;

        // 收藏的景点标签
        try {
            List<Favorite> favorites = favoriteService.getUserFavorites(userId);
            for (Favorite fav : favorites) {
                ScenicSpot spot = fav.getScenicSpot();
                if (spot == null) continue;
                for (String tag : parseTags(spot)) {
                    profile.merge(tag, 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}

        // 评分过的景点标签（高分景点权重加倍）
        if (reviewService != null) {
            try {
                List<com.jdz.tourism.entity.Review> reviews =
                        reviewService.getReviewsByUserId(userId);
                for (com.jdz.tourism.entity.Review review : reviews) {
                    ScenicSpot spot = review.getScenicSpot();
                    if (spot == null) continue;
                    int weight = (review.getRating() != null && review.getRating() >= 4) ? 2 : 1;
                    for (String tag : parseTags(spot)) {
                        profile.merge(tag, weight, Integer::sum);
                    }
                }
            } catch (Exception ignored) {}
        }

        return profile;
    }

    /**
     * Jaccard 相似度：|用户偏好标签 ∩ 景点标签| / |用户偏好标签 ∪ 景点标签|
     * 用户画像中标签频次越高，交集权重越大
     */
    private double jaccardSimilarity(Map<String, Integer> userProfile, Set<String> spotTags) {
        if (userProfile.isEmpty() || spotTags.isEmpty()) return 0.0;

        double intersection = 0.0;
        for (String tag : spotTags) {
            if (userProfile.containsKey(tag)) {
                intersection += userProfile.get(tag);
            }
        }

        // 并集 = 用户画像总权重 + 景点标签数 - 交集
        double union = userProfile.values().stream().mapToInt(Integer::intValue).sum()
                + spotTags.size() - intersection;

        return union == 0 ? 0.0 : intersection / union;
    }

    /**
     * 解析景点标签（tags + category 合并，逗号/空格分隔）
     */
    private Set<String> parseTags(ScenicSpot spot) {
        Set<String> tags = new HashSet<>();
        if (spot.getTags() != null && !spot.getTags().isBlank()) {
            for (String t : spot.getTags().split("[,，\\s]+")) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) tags.add(trimmed);
            }
        }
        if (spot.getCategory() != null && !spot.getCategory().isBlank()) {
            tags.add(spot.getCategory().trim());
        }
        return tags;
    }

    /**
     * 获取用户已收藏 + 已评分的景点 ID 集合（用于降权）
     */
    private Set<Long> getInteractedScenicIds(Long userId) {
        Set<Long> ids = new HashSet<>();
        if (userId == null) return ids;
        try {
            favoriteService.getUserFavorites(userId).stream()
                    .filter(f -> f.getScenicSpot() != null)
                    .forEach(f -> ids.add(f.getScenicSpot().getId()));
        } catch (Exception ignored) {}
        if (reviewService != null) {
            try {
                reviewService.getReviewsByUserId(userId).stream()
                        .filter(r -> r.getScenicSpot() != null)
                        .forEach(r -> ids.add(r.getScenicSpot().getId()));
            } catch (Exception ignored) {}
        }
        return ids;
    }
    
    /**
     * 将Merchant转换为ScenicSpot（统一转换逻辑）
     */
    private ScenicSpot convertMerchantToScenicSpot(Merchant m) {
        if (m == null) {
            System.out.println("convertMerchantToScenicSpot: Merchant为null");
            return null;
        }
        if (!"SCENIC".equals(m.getCategory())) {
            System.out.println("convertMerchantToScenicSpot: 商家分类不是SCENIC，当前分类: " + m.getCategory());
            return null;
        }
        
        Map<String, Object> enriched = merchantService.enrichMerchantWithRating(m);
        Boolean approved = (Boolean) enriched.get("qualificationApproved");
        
        // 获取评分信息（优先使用enriched中的评分，如果没有则从关联的ScenicSpot获取）
        Object ratingObj = enriched.get("rating");
        java.math.BigDecimal rating = null;
        if (ratingObj != null) {
            if (ratingObj instanceof Double) {
                rating = java.math.BigDecimal.valueOf((Double) ratingObj).setScale(2, java.math.RoundingMode.HALF_UP);
            } else if (ratingObj instanceof java.math.BigDecimal) {
                rating = (java.math.BigDecimal) ratingObj;
            } else if (ratingObj instanceof Number) {
                rating = java.math.BigDecimal.valueOf(((Number) ratingObj).doubleValue()).setScale(2, java.math.RoundingMode.HALF_UP);
            }
        }
        
        ScenicSpot s = m.getScenicSpot();
        if (s != null) {
            // 关联了已有的景点，从数据库重新查询完整数据（避免JPA懒加载代理导致字段为null）
            ScenicSpot fresh = scenicSpotRepository.findById(s.getId()).orElse(s);
            fresh.setQualificationApproved(approved);
            // 用商家地址/电话覆盖（商家数据更准确）
            if (m.getAddress() != null && !m.getAddress().isBlank()) {
                fresh.setAddress(m.getAddress());
            }
            if (m.getPhone() != null && !m.getPhone().isBlank()) {
                fresh.setPhone(m.getPhone());
            }
            // 优先使用商家的介绍字段，若景点表中无介绍则用商家介绍补充
            if ((fresh.getDescription() == null || fresh.getDescription().isBlank()) && m.getDescription() != null) {
                fresh.setDescription(m.getDescription());
            }
            // 评分处理：景点表有手动评分则保留，否则用评论计算的评分
            if (fresh.getRating() == null || fresh.getRating().compareTo(java.math.BigDecimal.ZERO) == 0) {
                if (rating != null && rating.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    fresh.setRating(rating);
                }
            }
            if (enriched.containsKey("activities")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> activities = (List<Map<String, Object>>) enriched.get("activities");
                fresh.setActivities(activities);
            }
            return fresh;
        } else {
            // 商家未关联景点，从商家信息构建景点对象
            s = new ScenicSpot();
            s.setId(m.getId());
            s.setName(m.getShopName());
            s.setDescription(m.getDescription());
            s.setCategory("SCENIC");
            s.setOpenTime(m.getOpenTime());
            s.setAddress(m.getAddress());
            s.setPhone(m.getPhone());
            
            // 处理图片
            if (m.getAvatar() != null && !m.getAvatar().isEmpty()) {
                s.setImage(m.getAvatar());
            } else if (m.getShopImages() != null && !m.getShopImages().isEmpty()) {
                String[] imgs = m.getShopImages().split(",");
                if (imgs.length > 0) s.setImage(imgs[0].trim());
            }
            s.setScenicImages(m.getShopImages());
            
            s.setPrice(m.getStartPrice() != null ? m.getStartPrice() : java.math.BigDecimal.ZERO);
            s.setVisitCount(0L);
            s.setTags("景点,商家");
            s.setQualificationApproved(approved);
            
            // 设置评分（从enriched中获取）
            if (rating != null) {
                s.setRating(rating);
            }
            
            // 补充活动信息
            if (enriched.containsKey("activities")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> activities = (List<Map<String, Object>>) enriched.get("activities");
                s.setActivities(activities);
            }
            return s;
        }
    }

    /**
     * 获取所有景点（包含系统景点和商家景点）
     * 统一通过MerchantService查询SCENIC分类
     */
    private List<ScenicSpot> getAllScenicSpotsIncludingMerchants() {
        // 1. 通过MerchantService获取SCENIC分类的商家
        List<Merchant> merchants = merchantService.getMerchantsByCategory("SCENIC");
        List<ScenicSpot> merchantSpots = merchants.stream()
                .map(this::convertMerchantToScenicSpot)
                .filter(spot -> spot != null)
                .peek(this::calculateRatingIfNeeded)
                .collect(Collectors.toList());
        
        // 2. 获取所有系统景点（向后兼容）
        List<ScenicSpot> systemSpots = scenicSpotRepository.findAll();
        systemSpots.forEach(this::calculateRatingIfNeeded);
        
        // 3. 合并（商家景点覆盖系统景点，因为包含审核状态）
        // 但确保评分一致性：如果系统景点有手动设置的评分，优先使用它
        Map<Long, ScenicSpot> merged = new HashMap<>();
        systemSpots.forEach(s -> merged.put(s.getId(), s));
        merchantSpots.forEach(s -> {
            // 如果系统景点表中也有这个景点，且系统景点有手动设置的评分，使用系统景点的评分
            ScenicSpot systemSpot = merged.get(s.getId());
            if (systemSpot != null && systemSpot.getRating() != null && 
                systemSpot.getRating().compareTo(java.math.BigDecimal.ZERO) > 0) {
                s.setRating(systemSpot.getRating());
            }
            merged.put(s.getId(), s);
        });
        
        return new ArrayList<>(merged.values());
    }

    /**
     * 为景点列表填充商家审核状态和活动信息
     * 统一通过MerchantService查询
     */
    private void enrichSpotsWithMerchantStatus(List<ScenicSpot> spots) {
        // 获取所有SCENIC分类的商家
        List<Merchant> merchants = merchantService.getMerchantsByCategory("SCENIC");
        Map<Long, Map<String, Object>> scenicIdToInfo = new HashMap<>();
        
        for (Merchant m : merchants) {
            Map<String, Object> enriched = merchantService.enrichMerchantWithRating(m);
            // 如果关联了景点，使用景点ID；否则使用商家ID
            Long key = m.getScenicSpot() != null ? m.getScenicSpot().getId() : m.getId();
            scenicIdToInfo.put(key, enriched);
        }
        
        // 填充状态和活动
        for (ScenicSpot spot : spots) {
            Long key = spot.getId();
            if (scenicIdToInfo.containsKey(key)) {
                Map<String, Object> info = scenicIdToInfo.get(key);
                spot.setQualificationApproved((Boolean) info.get("qualificationApproved"));
                
                if (info.containsKey("activities")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> activities = (List<Map<String, Object>>) info.get("activities");
                    spot.setActivities(activities);
                }
                
                // 填充关联的用户信息
                if (info.containsKey("user")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userMap = (Map<String, Object>) info.get("user");
                    User user = new User();
                    user.setId((Long) userMap.get("id"));
                    user.setUsername((String) userMap.get("username"));
                    spot.setUser(user);
                }
            }
        }
    }


    
    /**
     * 如果景点未设置图片，为特定名称补默认图片
     */
    private ScenicSpot withDefaultImage(ScenicSpot spot) {
        if (spot == null) {
            return null;
        }
        if (spot.getImage() == null || spot.getImage().isBlank()) {
            Map<String, String> defaultImages = Map.ofEntries(
                    Map.entry("景德镇古窑民俗博览区", "/images/scenic/scenic_1.jpg"),
                    Map.entry("中国陶瓷博物馆", "/images/scenic/scenic_2.jpg"),
                    Map.entry("瑶里古镇", "/images/scenic/scenic_3.jpg"),
                    Map.entry("浮梁古县衙", "/images/scenic/scenic_4.jpg"),
                    Map.entry("高岭国家矿山公园", "/images/scenic/scenic_5.jpg"),
                    Map.entry("瓷宫", "/images/scenic/scenic_6.jpg"),
                    Map.entry("柴窑", "/images/scenic/scenic_7.jpg")
            );
            String path = defaultImages.getOrDefault(spot.getName(), "/images/scenic/default.jpg");
            spot.setImage(path);
        }
        return spot;
    }
    
    /**
     * 创建景点
     */
    /**
     * 创建景点，并自动创建对应的商家记录
     */
    public ScenicSpot createScenicSpot(ScenicSpot scenicSpot) {
        // 检查是否已存在同名景点（防止重复创建）
        if (scenicSpot.getName() != null && !scenicSpot.getName().trim().isEmpty()) {
            Optional<ScenicSpot> existing = getScenicSpotByName(scenicSpot.getName());
            if (existing.isPresent()) {
                throw new RuntimeException("景点名称 '" + scenicSpot.getName() + "' 已存在，无法创建重复景点。请使用编辑功能更新现有景点。");
            }
        }
        
        // 如果提供了ID，说明这是更新操作，不应该调用create方法
        if (scenicSpot.getId() != null && scenicSpot.getId() > 0) {
            throw new RuntimeException("景点ID已存在（ID: " + scenicSpot.getId() + "），请使用更新方法而不是创建方法。");
        }
        
        // 保存景点
        ScenicSpot savedScenicSpot = scenicSpotRepository.save(scenicSpot);
        
        // 自动为景点创建对应的商家记录（如果不存在）
        // 统一使用MerchantService的createMerchantForScenicSpot方法
        try {
            merchantService.createMerchantForScenicSpot(savedScenicSpot);
            System.out.println("已为景点 " + savedScenicSpot.getId() + " 自动创建商家记录");
        } catch (Exception e) {
            // 如果商家已存在或其他错误，记录日志但不影响景点创建
            System.err.println("为景点创建商家记录时发生错误（可能已存在）: " + e.getMessage());
        }
        
        return savedScenicSpot;
    }
    
    /**
     * 更新景点
     * 确保编辑时不会新增景点，景点就是商家（SCENIC分类）
     * 支持更新：
     * 1. 关联了scenic_spot表的商家（通过scenic_id关联）
     * 2. 新注册的景点商家（scenic_id=null，直接是商家ID）
     */
    public ScenicSpot updateScenicSpot(ScenicSpot scenicSpot) {
        if (scenicSpot.getId() == null) {
            throw new RuntimeException("景点ID不能为空，无法更新");
        }
        
        Long id = scenicSpot.getId();
        System.out.println("========== 开始更新景点/商家，ID = " + id + " ==========");
        
        // 1. 先尝试通过商家ID查找（可能是新注册的景点商家，scenic_id=null）
        Optional<Merchant> merchantOpt = merchantService.getMerchantById(id);
        if (!merchantOpt.isPresent()) {
            // 2. 如果找不到，尝试通过景点ID查找关联的商家
            merchantOpt = merchantService.getMerchantByScenicSpotId(id);
        }
        
        // 3. 如果找到了商家（SCENIC分类），直接更新商家信息
        if (merchantOpt.isPresent()) {
            Merchant merchant = merchantOpt.get();
            System.out.println("找到商家: ID=" + merchant.getId() + ", category=" + merchant.getCategory() + ", scenic_id=" + (merchant.getScenicSpot() != null ? merchant.getScenicSpot().getId() : "null"));
            
            if ("SCENIC".equals(merchant.getCategory())) {
                // 更新商家信息（包括空值，允许清空字段）
                merchant.setShopName(scenicSpot.getName());
                merchant.setDescription(scenicSpot.getDescription());
                if (scenicSpot.getPrice() != null) {
                    merchant.setStartPrice(scenicSpot.getPrice());
                }
                merchant.setOpenTime(scenicSpot.getOpenTime());
                merchant.setAddress(scenicSpot.getAddress());
                merchant.setPhone(scenicSpot.getPhone());
                merchant.setShopImages(scenicSpot.getScenicImages());
                merchant.setAvatar(scenicSpot.getImage());
                // 商家侧暂不区分官方电话/公告明细
                
                merchantService.updateMerchant(merchant);
                System.out.println("已更新商家，商家ID: " + merchant.getId());
                
                // 如果关联了景点，同时更新景点信息
                if (merchant.getScenicSpot() != null) {
                    ScenicSpot systemSpot = merchant.getScenicSpot();
                    // 更新所有字段，包括空值（允许清空字段）
                    systemSpot.setName(scenicSpot.getName());
                    systemSpot.setDescription(scenicSpot.getDescription());
                    if (scenicSpot.getPrice() != null) {
                        systemSpot.setPrice(scenicSpot.getPrice());
                    }
                    systemSpot.setOpenTime(scenicSpot.getOpenTime());
                    systemSpot.setAddress(scenicSpot.getAddress());
                    systemSpot.setPhone(scenicSpot.getPhone());
                    systemSpot.setOfficialPhone(scenicSpot.getOfficialPhone());
                    systemSpot.setOpeningHoursDetail(scenicSpot.getOpeningHoursDetail());
                    systemSpot.setNotice(scenicSpot.getNotice());
                    systemSpot.setNoticeDetail(scenicSpot.getNoticeDetail());
                    systemSpot.setPreferentialPolicy(scenicSpot.getPreferentialPolicy());
                    systemSpot.setFacilitiesDetail(scenicSpot.getFacilitiesDetail());
                    systemSpot.setTicketInfo(scenicSpot.getTicketInfo());
                    systemSpot.setFeatures(scenicSpot.getFeatures());
                    systemSpot.setHighlights(scenicSpot.getHighlights());
                    systemSpot.setTips(scenicSpot.getTips());
                    systemSpot.setCategory(scenicSpot.getCategory());
                    systemSpot.setTags(scenicSpot.getTags());
                    systemSpot.setLatitude(scenicSpot.getLatitude());
                    systemSpot.setLongitude(scenicSpot.getLongitude());
                    systemSpot.setImage(scenicSpot.getImage());
                    systemSpot.setScenicImages(scenicSpot.getScenicImages());
                    // 评分：如果提供了评分则更新，否则保持原样（不自动计算，避免覆盖手动设置的评分）
                    if (scenicSpot.getRating() != null) {
                        systemSpot.setRating(scenicSpot.getRating());
                    }
                    scenicSpotRepository.save(systemSpot);
                    System.out.println("已同步更新关联景点，景点ID: " + systemSpot.getId());
                    return systemSpot;
                } else {
                    // 新注册的景点商家（scenic_id=null），从商家信息构建景点对象返回
                    ScenicSpot spot = convertMerchantToScenicSpot(merchant);
                    calculateRatingIfNeeded(spot);
                    return spot;
                }
            }
        }
        
        // 4. 向后兼容：尝试从系统景点表查询并更新
        Optional<ScenicSpot> existingOpt = scenicSpotRepository.findById(id);
        if (existingOpt.isPresent()) {
            ScenicSpot existing = existingOpt.get();
            
            // 更新所有字段，包括空值（允许清空字段）
            existing.setName(scenicSpot.getName());
            existing.setDescription(scenicSpot.getDescription());
            if (scenicSpot.getPrice() != null) {
                existing.setPrice(scenicSpot.getPrice());
            }
            existing.setOpenTime(scenicSpot.getOpenTime());
            existing.setAddress(scenicSpot.getAddress());
            existing.setPhone(scenicSpot.getPhone());
            existing.setOfficialPhone(scenicSpot.getOfficialPhone());
            existing.setOpeningHoursDetail(scenicSpot.getOpeningHoursDetail());
            existing.setNotice(scenicSpot.getNotice());
            existing.setNoticeDetail(scenicSpot.getNoticeDetail());
            existing.setPreferentialPolicy(scenicSpot.getPreferentialPolicy());
            existing.setFacilitiesDetail(scenicSpot.getFacilitiesDetail());
            existing.setTicketInfo(scenicSpot.getTicketInfo());
            existing.setFeatures(scenicSpot.getFeatures());
            existing.setHighlights(scenicSpot.getHighlights());
            existing.setTips(scenicSpot.getTips());
            existing.setCategory(scenicSpot.getCategory());
            existing.setTags(scenicSpot.getTags());
            existing.setLatitude(scenicSpot.getLatitude());
            existing.setLongitude(scenicSpot.getLongitude());
            existing.setImage(scenicSpot.getImage());
            existing.setScenicImages(scenicSpot.getScenicImages());
            // 评分：如果提供了评分则更新，否则保持原样
            if (scenicSpot.getRating() != null) {
                existing.setRating(scenicSpot.getRating());
            }
            
            ScenicSpot updated = scenicSpotRepository.save(existing);
            System.out.println("已更新系统景点，景点ID: " + id);
            return updated;
        }
        
        // 5. 如果都找不到，抛出异常，不允许创建新景点
        throw new RuntimeException("景点或商家不存在，ID: " + id + "。无法更新不存在的景点，请确保景点ID正确。如果是新增景点，请使用创建接口。");
    }
    
    /**
     * 删除景点关联的数据（订单、收藏、评论）
     * 必须在删除景点之前调用，否则会触发外键约束错误
     */
    private void deleteRelatedData(Long scenicSpotId) {
        // 删除关联的订单（必须成功，否则会触发外键约束）
        // 先获取订单列表，以便在删除失败时能够将 scenic_id 设置为 null
        List<com.jdz.tourism.entity.Order> orders = orderService.getOrdersByScenicSpotId(scenicSpotId);
        
        if (!orders.isEmpty()) {
            try {
                orderService.deleteOrdersByScenicSpotId(scenicSpotId);
                System.out.println("已删除景点 " + scenicSpotId + " 的关联订单（共 " + orders.size() + " 条）");
            } catch (Exception e) {
                System.err.println("删除景点订单失败: " + e.getMessage());
                // 如果删除失败，尝试将订单的 scenic_id 设置为 null（更温和的处理方式）
                try {
                    for (com.jdz.tourism.entity.Order order : orders) {
                        order.setScenicSpot(null);
                        orderService.updateOrder(order);
                    }
                    System.out.println("已将景点 " + scenicSpotId + " 的关联订单的 scenic_id 设置为 null（共 " + orders.size() + " 条）");
                } catch (Exception e2) {
                    System.err.println("设置订单 scenic_id 为 null 失败: " + e2.getMessage());
                    throw new RuntimeException("删除景点失败：无法删除或更新关联的订单（共 " + orders.size() + " 条）。请先处理相关订单后再删除景点。", e);
                }
            }
        } else {
            System.out.println("景点 " + scenicSpotId + " 没有关联的订单");
        }
        
        // 删除关联的收藏
        try {
            favoriteService.deleteFavoritesByScenicSpotId(scenicSpotId);
            System.out.println("已删除景点 " + scenicSpotId + " 的关联收藏");
        } catch (Exception e) {
            System.err.println("删除景点收藏失败: " + e.getMessage());
            // 收藏删除失败不影响删除景点，只记录日志
        }
        
        // 删除关联的评论
        if (reviewService != null) {
            try {
                reviewService.deleteReviewsByScenicSpotId(scenicSpotId);
                System.out.println("已删除景点 " + scenicSpotId + " 的关联评论");
            } catch (Exception e) {
                System.err.println("删除景点评论失败: " + e.getMessage());
                // 评论删除失败不影响删除景点，只记录日志
            }
        }
    }
    
    /**
     * 删除景点
     * 统一处理景点和关联的商家记录（与餐饮、酒店、陶瓷工坊逻辑一致）
     * 支持通过景点ID或商家ID删除
     */
    @org.springframework.transaction.annotation.Transactional
    public void deleteScenicSpot(Long id) {
        System.out.println("========== 开始删除景点/商家，ID = " + id + " ==========");
        
        // 1. 先尝试通过商家ID查找（可能是商家ID）
        Optional<Merchant> merchantOpt = merchantService.getMerchantById(id);
        if (!merchantOpt.isPresent()) {
            // 2. 如果找不到，尝试通过景点ID查找关联的商家
            merchantOpt = merchantService.getMerchantByScenicSpotId(id);
        }
        
        if (merchantOpt.isPresent()) {
            Merchant merchant = merchantOpt.get();
            System.out.println("找到关联商家: ID=" + merchant.getId() + ", category=" + merchant.getCategory() + ", scenic_id=" + (merchant.getScenicSpot() != null ? merchant.getScenicSpot().getId() : "null"));
            
            // 如果是SCENIC分类的商家，删除商家记录
            if ("SCENIC".equals(merchant.getCategory())) {
                // 如果关联了景点，先删除关联数据，再删除景点
                if (merchant.getScenicSpot() != null) {
                    Long scenicId = merchant.getScenicSpot().getId();
                    System.out.println("删除关联的景点: " + scenicId);
                    // 先删除关联的订单、收藏、评论
                    deleteRelatedData(scenicId);
                    // 然后删除景点
                    scenicSpotRepository.deleteById(scenicId);
                }
                
                // 删除商家记录
                merchantService.deleteMerchant(merchant.getId());
                System.out.println("已删除景点商家，商家ID = " + merchant.getId());
                return;
            } else {
                // 如果不是SCENIC分类，只清空关联关系
                if (merchant.getScenicSpot() != null && merchant.getScenicSpot().getId().equals(id)) {
                    merchant.setScenicSpot(null);
                    merchantService.updateMerchant(merchant);
                    System.out.println("已清空商家 " + merchant.getId() + " 的景点关联");
                }
            }
        }
        
        // 3. 删除景点（如果存在）
        if (scenicSpotRepository.existsById(id)) {
            // 先删除关联的订单、收藏、评论
            deleteRelatedData(id);
            
            // 然后删除景点
            scenicSpotRepository.deleteById(id);
            System.out.println("已删除景点，ID = " + id);
        } else {
            System.out.println("景点不存在，ID = " + id);
        }
        
        System.out.println("========== 删除完成 ==========");
    }
    
    private boolean filterByCategory(ScenicSpot spot, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }
        String filter = category.trim().toLowerCase();
        String spotCategory = spot.getCategory() == null ? "" : spot.getCategory().trim().toLowerCase();
        String spotTags = spot.getTags() == null ? "" : spot.getTags().toLowerCase();

        // 直接匹配或包含匹配，兼容“文化景区”与“景区”/“文化”
        if (spotCategory.equalsIgnoreCase(filter)
                || spotCategory.contains(filter)
                || filter.contains(spotCategory)
                || spotTags.contains(filter)) {
            return true;
        }

        // 针对常见中文分类做简易同义兼容
        if (filter.contains("景区") && spotCategory.contains("景区")) return true;
        if (filter.contains("文化") && spotCategory.contains("文化")) return true;
        if (filter.contains("博物馆") && spotCategory.contains("博物馆")) return true;
        if (filter.contains("古镇") && spotCategory.contains("古镇")) return true;
        if (filter.contains("陶瓷") && (spotCategory.contains("陶瓷") || spotTags.contains("陶瓷"))) return true;

        return false;
    }

    private boolean filterByPriceRange(ScenicSpot spot, String priceRange) {
        if (priceRange == null || priceRange.isBlank() || spot.getPrice() == null) {
            return true;
        }
        switch (priceRange) {
            case "free":
                return spot.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0;
            case "0-50":
                return spot.getPrice().compareTo(java.math.BigDecimal.ZERO) >= 0
                        && spot.getPrice().compareTo(java.math.BigDecimal.valueOf(50)) <= 0;
            case "50-100":
                return spot.getPrice().compareTo(java.math.BigDecimal.valueOf(50)) >= 0
                        && spot.getPrice().compareTo(java.math.BigDecimal.valueOf(100)) <= 0;
            case "100+":
                return spot.getPrice().compareTo(java.math.BigDecimal.valueOf(100)) >= 0;
            default:
                return true;
        }
    }
    
    /**
     * 对推荐结果添加随机性，增加推荐的多样性
     * 保持主要排序逻辑，但对结果进行适度随机化
     */
    private List<ScenicSpot> addRandomnessToRecommendation(List<ScenicSpot> sortedList, String sort) {
        if (sortedList == null || sortedList.size() <= 1) {
            return sortedList;
        }
        
        // 创建一个新的列表，避免修改原列表
        List<ScenicSpot> result = new ArrayList<>(sortedList);
        
        // 对于推荐度和热门度排序，保持前几个热门景点的顺序，对后面的进行随机打乱
        // 这样既保证了热门景点的优先展示，又增加了推荐的多样性
        if ("recommend".equalsIgnoreCase(sort) || "popularity".equalsIgnoreCase(sort)) {
            // 保持前2-3个最热门的景点顺序，对后面的进行随机打乱
            int keepTop = Math.min(3, result.size() / 2); // 保持前一半或前3个，取较小值
            if (result.size() > keepTop && keepTop > 0) {
                List<ScenicSpot> topSpots = new ArrayList<>(result.subList(0, keepTop));
                List<ScenicSpot> rest = new ArrayList<>(result.subList(keepTop, result.size()));
                Collections.shuffle(rest, new Random());
                result = new ArrayList<>(topSpots);
                result.addAll(rest);
            } else {
                // 如果结果较少，全部打乱
                Collections.shuffle(result, new Random());
            }
        } else {
            // 对于其他排序方式（如价格），进行完全随机打乱
            // 这样可以增加每次推荐的多样性
            Collections.shuffle(result, new Random());
        }
        
        return result;
    }

    /**
     * 计算景点评分（如果未手动设置）
     * 如果rating为null，则从用户评论计算平均评分
     */
    private void calculateRatingIfNeeded(ScenicSpot spot) {
        if (spot == null || spot.getId() == null) {
            return;
        }
        
        // 如果已经手动设置了评分（非null且大于0），则使用手动设置的评分
        if (spot.getRating() != null && spot.getRating().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return;
        }
        
        // 如果没有手动设置评分，且ReviewService可用，则从评论计算平均评分
        if (reviewService != null) {
            try {
                Double avgRating = reviewService.getAverageRatingByScenicSpotId(spot.getId());
                if (avgRating != null && avgRating > 0) {
                    // 保留2位小数
                    spot.setRating(java.math.BigDecimal.valueOf(avgRating).setScale(2, java.math.RoundingMode.HALF_UP));
                } else {
                    // 如果没有评论，设置为0
                    spot.setRating(java.math.BigDecimal.ZERO);
                }
            } catch (Exception e) {
                // 如果计算失败，设置为0
                System.err.println("计算景点评分失败，ID: " + spot.getId() + ", 错误: " + e.getMessage());
                spot.setRating(java.math.BigDecimal.ZERO);
            }
        } else {
            // 如果ReviewService不可用，设置为0
            spot.setRating(java.math.BigDecimal.ZERO);
        }
    }

    /**
     * 计算并填充评论数量（优先按scenic_spot表ID统计，不存在则按商家ID统计）
     */
    private void fillCommentCount(ScenicSpot spot) {
        if (spot == null || spot.getId() == null) return;
        try {
            Long scenicId = spot.getId();
            Long count = 0L;
            boolean scenicExists = scenicSpotRepository.existsById(scenicId);
            if (reviewService != null) {
                if (scenicExists) {
                    count = reviewService.getReviewCountByScenicSpotId(scenicId);
                } else {
                    count = reviewService.getReviewCountByMerchantId(scenicId);
                }
            }
            spot.setCommentCount(count != null ? count : 0L);
        } catch (Exception e) {
            spot.setCommentCount(0L);
        }
    }
}