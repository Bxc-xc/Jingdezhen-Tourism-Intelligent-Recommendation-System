package com.jdz.tourism.service;

import com.jdz.tourism.entity.TravelRoute;
import com.jdz.tourism.repository.TravelRouteRepository;
import com.jdz.tourism.repository.MerchantFavoriteRepository;
import com.jdz.tourism.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 旅游路线服务
 * 增强版：支持热门路线推荐算法优化
 */
@Service
public class TravelRouteService {
    
    @Autowired
    private TravelRouteRepository travelRouteRepository;

    @Autowired(required = false)
    private MerchantFavoriteRepository merchantFavoriteRepository;

    @Autowired(required = false)
    @Lazy
    private ReviewService reviewService;

    @Autowired(required = false)
    @Lazy
    private com.jdz.tourism.repository.FavoriteRepository favoriteRepository;

    @Autowired(required = false)
    private SystemConfigService systemConfigService;
    
    public List<TravelRoute> getAllRoutes() {
        return travelRouteRepository.findAll();
    }
    
    public Optional<TravelRoute> getRouteById(Long id) {
        return travelRouteRepository.findById(id);
    }
    
    public List<TravelRoute> getRoutesByDays(Integer days) {
        List<TravelRoute> routes = travelRouteRepository.findByDays(days);
        // 按热门度排序（可以根据访问量、评分等）
        return sortRoutesByPopularity(routes);
    }
    
    public List<TravelRoute> getRoutesByDifficulty(String difficulty) {
        List<TravelRoute> routes = travelRouteRepository.findByDifficulty(difficulty);
        return sortRoutesByPopularity(routes);
    }
    
    /**
     * 获取热门推荐路线（增强版）
     * 综合考虑天数、难度、价格等因素进行智能推荐
     */
    public List<TravelRoute> getPopularRoutes(Integer days, String difficulty) {
        List<TravelRoute> routes;
        
        if (days != null && days > 0) {
            routes = travelRouteRepository.findByDays(days);
        } else if (difficulty != null && !difficulty.isEmpty()) {
            routes = travelRouteRepository.findByDifficulty(difficulty);
        } else {
            routes = travelRouteRepository.findAll();
        }
        
        // 应用智能推荐算法
        return recommendRoutes(routes, days, difficulty);
    }
    
    /**
     * 智能推荐路线算法
     * 综合考虑多个因素：
     * 1. 路线完整性（景点数量）
     * 2. 价格合理性
     * 3. 标签匹配度
     * 4. 天数匹配度
     */
    private List<TravelRoute> recommendRoutes(List<TravelRoute> routes, Integer preferredDays, String preferredDifficulty) {
        return routes.stream()
            .sorted(Comparator
                .comparing((TravelRoute route) -> calculateRouteScore(route, preferredDays, preferredDifficulty))
                .reversed()) // 降序排列
            .limit(10) // 返回前10条推荐路线
            .collect(Collectors.toList());
    }
    
    /**
     * 计算路线推荐分数
     */
    private double calculateRouteScore(TravelRoute route, Integer preferredDays, String preferredDifficulty) {
        double score = 0.0;
        
        // 1. 天数匹配度（如果用户指定了天数）
        if (preferredDays != null && route.getDays() != null) {
            int dayDiff = Math.abs(route.getDays() - preferredDays);
            score += (3 - dayDiff) * 10; // 天数越接近，分数越高
        }
        
        // 2. 难度匹配度
        if (preferredDifficulty != null && route.getDifficulty() != null) {
            if (route.getDifficulty().equalsIgnoreCase(preferredDifficulty)) {
                score += 20;
            }
        }
        
        // 3. 价格合理性（价格越低，分数越高，但也要考虑质量）
        if (route.getTotalPrice() != null) {
            double price = route.getTotalPrice().doubleValue();
            if (price == 0) {
                score += 15; // 免费路线加分
            } else if (price < 100) {
                score += 10;
            } else if (price < 300) {
                score += 5;
            }
        }
        
        // 4. 景点数量（景点越多，路线越完整）
        if (route.getScenicSpots() != null && !route.getScenicSpots().isEmpty()) {
            try {
                // 简单估算景点数量（通过JSON字符串长度）
                int spotCount = route.getScenicSpots().split("\\{").length - 1;
                score += Math.min(spotCount * 5, 30); // 最多30分
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        
        // 5. 标签丰富度
        if (route.getTags() != null && !route.getTags().isEmpty()) {
            int tagCount = route.getTags().split(",").length;
            score += Math.min(tagCount * 2, 10); // 最多10分
        }
        
        return score;
    }
    
    /**
     * 按热门度排序路线
     */
    private List<TravelRoute> sortRoutesByPopularity(List<TravelRoute> routes) {
        return routes.stream()
            .sorted(Comparator
                .comparing((TravelRoute route) -> {
                    // 优先显示有图片的路线
                    if (route.getImage() != null && !route.getImage().isEmpty()) {
                        return 1;
                    }
                    return 0;
                })
                .thenComparing((TravelRoute route) -> {
                    // 其次按价格排序（价格合理的优先）
                    if (route.getTotalPrice() != null) {
                        return route.getTotalPrice().doubleValue();
                    }
                    return Double.MAX_VALUE;
                })
                .thenComparing((TravelRoute route) -> {
                    // 最后按天数排序（天数少的优先）
                    if (route.getDays() != null) {
                        return route.getDays();
                    }
                    return Integer.MAX_VALUE;
                }))
            .collect(Collectors.toList());
    }
    
    public TravelRoute saveRoute(TravelRoute route) {
        return travelRouteRepository.save(route);
    }
    
    public TravelRoute createRoute(TravelRoute route) {
        return travelRouteRepository.save(route);
    }
    
    public TravelRoute updateRoute(TravelRoute route) {
        return travelRouteRepository.save(route);
    }
    
    public void deleteRoute(Long id) {
        travelRouteRepository.deleteById(id);
    }

    // ==================== 智能推荐算法 ====================

    /**
     * 基于用户画像的路线个性化推荐
     * 冷启动时降级为规则评分排序
     */
    public List<TravelRoute> recommendRoutesByUser(Long userId, Integer days, String difficulty) {
        List<TravelRoute> routes = getPopularRoutes(days, difficulty);
        if (routes.isEmpty()) return routes;

        Map<String, Integer> profile = buildRouteUserProfile(userId);
        if (profile.isEmpty()) return routes;

        double maxScore = routes.stream()
                .mapToDouble(r -> calculateRouteScore(r, days, difficulty))
                .max().orElse(1.0);
        if (maxScore == 0) maxScore = 1.0;
        final double ms = maxScore;

        routes.sort((a, b) -> {
            double simA = jaccardRoute(profile, parseRouteTags(a));
            double popA = calculateRouteScore(a, days, difficulty) / ms;
            double scoreA = simA * 0.7 + popA * 0.3;

            double simB = jaccardRoute(profile, parseRouteTags(b));
            double popB = calculateRouteScore(b, days, difficulty) / ms;
            double scoreB = simB * 0.7 + popB * 0.3;

            return Double.compare(scoreB, scoreA);
        });
        return routes;
    }

    private Map<String, Integer> buildRouteUserProfile(Long userId) {
        Map<String, Integer> profile = new HashMap<>();
        if (userId == null) return profile;

        if (favoriteRepository != null) {
            try {
                favoriteRepository.findByUserId(userId).forEach(f -> {
                    if (f.getScenicSpot() == null) return;
                    String tags = f.getScenicSpot().getTags();
                    if (tags != null) {
                        for (String t : tags.split("[,，\\s]+")) {
                            String s = t.trim(); if (!s.isEmpty()) profile.merge(s, 1, Integer::sum);
                        }
                    }
                });
            } catch (Exception ignored) {}
        }

        if (merchantFavoriteRepository != null) {
            try {
                merchantFavoriteRepository.findByUserId(userId).forEach(f -> {
                    if (f.getMerchant() == null) return;
                    String tags = f.getMerchant().getTags();
                    if (tags != null) {
                        for (String t : tags.split("[,，\\s]+")) {
                            String s = t.trim(); if (!s.isEmpty()) profile.merge(s, 1, Integer::sum);
                        }
                    }
                    String cat = f.getMerchant().getCategory();
                    if (cat != null) profile.merge(cat, 1, Integer::sum);
                });
            } catch (Exception ignored) {}
        }

        if (reviewService != null) {
            try {
                reviewService.getReviewsByUserId(userId).forEach(r -> {
                    int w = (r.getRating() != null && r.getRating() >= 4) ? 2 : 1;
                    if (r.getScenicSpot() != null && r.getScenicSpot().getTags() != null) {
                        for (String t : r.getScenicSpot().getTags().split("[,，\\s]+")) {
                            String s = t.trim(); if (!s.isEmpty()) profile.merge(s, w, Integer::sum);
                        }
                    }
                });
            } catch (Exception ignored) {}
        }
        return profile;
    }

    private double jaccardRoute(Map<String, Integer> profile, Set<String> tags) {
        if (profile.isEmpty() || tags.isEmpty()) return 0.0;
        double inter = 0.0;
        for (String t : tags) if (profile.containsKey(t)) inter += profile.get(t);
        double union = profile.values().stream().mapToInt(Integer::intValue).sum() + tags.size() - inter;
        return union == 0 ? 0.0 : inter / union;
    }

    private Set<String> parseRouteTags(TravelRoute route) {
        Set<String> tags = new HashSet<>();
        if (route.getTags() != null) {
            for (String t : route.getTags().split("[,，\\s]+")) {
                String s = t.trim(); if (!s.isEmpty()) tags.add(s);
            }
        }
        if (route.getDifficulty() != null) tags.add(route.getDifficulty());
        return tags;
    }
}

