package com.jdz.tourism.service;

import com.jdz.tourism.entity.Marketplace;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.repository.MarketplaceRepository;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.ScenicSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能行程生成服务
 * 核心算法：偏好筛选 + 贪心最近邻路线优化 + 按天分配
 */
@Service
public class SmartTripService {

    @Autowired
    private ScenicSpotRepository scenicSpotRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MarketplaceRepository marketplaceRepository;

    /**
     * 行程条目数据结构
     */
    public static class TripItem {
        public String type;   // scenic / food / hotel
        public Long   id;
        public String name;
        public double lat;
        public double lng;
        public String image;
        public String description;
        public String address;
        public double price;
        public double rating;
        public String tags;
        public String category;
    }

    /**
     * 单天行程
     */
    public static class DayPlan {
        public int day;
        public String date;
        public String note;
        public List<TripItem> items = new ArrayList<>();
    }

    /**
     * 生成智能行程
     *
     * @param days        旅游天数 1-5
     * @param preferences 偏好列表：scenic_tour / food_experience / culture / leisure
     * @param budget      预算档次：low / medium / high（可为 null）
     * @return 按天分组的行程列表
     */
    public List<DayPlan> generateTrip(int days, List<String> preferences, String budget, String startDate) {
        days = Math.max(1, Math.min(30, days));  // 支持最多30天

        // 1. 拉取候选数据
        List<TripItem> candidates = buildCandidates(preferences, budget);

        if (candidates.isEmpty()) {
            // 兜底：拉全部景点
            candidates = buildCandidates(Collections.emptyList(), null);
        }

        // 2. 贪心最近邻排序（从景德镇中心出发）
        List<TripItem> sorted = nearestNeighborSort(candidates, 29.2926, 117.1781);

        // 3. 按天分配，每天 2-4 个地点
        return assignToDays(sorted, days, startDate);
    }

    // ── 私有方法 ──────────────────────────────────────────────

    /**
     * 根据偏好和预算构建候选地点列表
     */
    private List<TripItem> buildCandidates(List<String> preferences, String budget) {
        List<TripItem> result = new ArrayList<>();
        boolean wantScenic  = preferences.isEmpty() || preferences.contains("scenic_tour")  || preferences.contains("culture");
        boolean wantFood    = preferences.isEmpty() || preferences.contains("food_experience");
        boolean wantLeisure = preferences.isEmpty() || preferences.contains("leisure");

        // 景点
        if (wantScenic || wantLeisure) {
            List<ScenicSpot> scenics = scenicSpotRepository.findAll();
            for (ScenicSpot s : scenics) {
                if (s.getLatitude() == null || s.getLongitude() == null) continue;
                // 预算过滤：low 只取免费/低价景点
                if ("low".equals(budget) && s.getPrice() != null && s.getPrice().doubleValue() > 80) continue;

                TripItem item = new TripItem();
                item.type        = "scenic";
                item.id          = s.getId();
                item.name        = s.getName();
                item.lat         = s.getLatitude().doubleValue();
                item.lng         = s.getLongitude().doubleValue();
                item.image       = firstImage(s.getScenicImages(), s.getImage());
                item.description = s.getDescription() != null
                        ? s.getDescription().substring(0, Math.min(60, s.getDescription().length()))
                        : "";
                item.address     = s.getAddress() != null ? s.getAddress() : "";
                item.price       = s.getPrice() != null ? s.getPrice().doubleValue() : 0;
                item.rating      = s.getRating() != null ? s.getRating().doubleValue() : 4.0;
                item.tags        = s.getTags() != null ? s.getTags() : "";
                item.category    = s.getCategory() != null ? s.getCategory() : "景点";
                result.add(item);
            }
        }

        // 美食商家（无真实坐标，不参与路线排序，附加到每天行程中）
        if (wantFood) {
            List<Merchant> foods = merchantRepository.findByCategoryAndDeletedFalse("FOOD");
            for (Merchant m : foods) {
                if (!Boolean.TRUE.equals(m.getEnabled())) continue;
                TripItem item = new TripItem();
                item.type        = "food";
                item.id          = m.getId();
                item.name        = m.getShopName();
                item.lat         = 0;  // 无真实坐标，不参与路线距离计算
                item.lng         = 0;
                item.image       = firstImage(m.getShopImages(), m.getAvatar());
                item.description = m.getDescription() != null
                        ? m.getDescription().substring(0, Math.min(60, m.getDescription().length()))
                        : "";
                item.address     = m.getAddress() != null ? m.getAddress() : "";
                item.price       = m.getStartPrice() != null ? m.getStartPrice().doubleValue() : 0;
                item.rating      = m.getAdminRating() != null ? m.getAdminRating() : 4.0;
                item.tags        = m.getTags() != null ? m.getTags() : "";
                item.category    = "美食";
                result.add(item);
            }
        }

        // 陶瓷工坊（CERAMIC）—— 无真实坐标，不参与路线排序
        List<Merchant> ceramics = merchantRepository.findByCategoryAndDeletedFalse("CERAMIC");
        for (Merchant m : ceramics) {
            if (!Boolean.TRUE.equals(m.getEnabled())) continue;
            TripItem item = new TripItem();
            item.type        = "ceramic";
            item.id          = m.getId();
            item.name        = m.getShopName();
            item.lat         = 0;
            item.lng         = 0;
            item.image       = firstImage(m.getShopImages(), m.getAvatar());
            item.description = m.getDescription() != null
                    ? m.getDescription().substring(0, Math.min(60, m.getDescription().length())) : "";
            item.address     = m.getAddress() != null ? m.getAddress() : "";
            item.price       = m.getStartPrice() != null ? m.getStartPrice().doubleValue() : 0;
            item.rating      = m.getAdminRating() != null ? m.getAdminRating() : 4.5;
            item.tags        = m.getTags() != null ? m.getTags() : "";
            item.category    = "陶瓷工坊";
            result.add(item);
        }

        // 陶瓷市集（marketplace 独立表）—— 无真实坐标，不参与路线排序
        List<Marketplace> markets = marketplaceRepository.findAllByEnabledTrueOrderBySortOrderAscIdAsc();
        for (Marketplace m : markets) {
            TripItem item = new TripItem();
            item.type        = "market";
            item.id          = m.getId();
            item.name        = m.getName();
            item.lat         = 0;
            item.lng         = 0;
            item.image       = firstImage(m.getCarouselImages(), m.getCoverImage());
            item.description = m.getDescription() != null
                    ? m.getDescription().substring(0, Math.min(60, m.getDescription().length())) : "";
            item.address     = m.getAddress() != null ? m.getAddress() : "";
            item.price       = 0;
            item.rating      = 4.5;
            item.tags        = "陶瓷市集";
            item.category    = "陶瓷市集";
            result.add(item);
        }

        // 酒店（每天末尾安排一个，最多取 10 家备用）
        List<Merchant> hotels = merchantRepository.findByCategoryAndDeletedFalse("HOTEL");
        int hotelCount = 0;
        for (Merchant m : hotels) {
            if (!Boolean.TRUE.equals(m.getEnabled())) continue;
            if (hotelCount >= 10) break;
            TripItem item = new TripItem();
            item.type        = "hotel";
            item.id          = m.getId();
            item.name        = m.getShopName();
            item.lat         = 0;
            item.lng         = 0;
            item.image       = firstImage(m.getShopImages(), m.getAvatar());
            item.description = m.getDescription() != null
                    ? m.getDescription().substring(0, Math.min(60, m.getDescription().length()))
                    : "";
            item.address     = m.getAddress() != null ? m.getAddress() : "";
            item.price       = m.getStartPrice() != null ? m.getStartPrice().doubleValue() : 0;
            item.rating      = m.getAdminRating() != null ? m.getAdminRating() : 4.0;
            item.tags        = m.getTags() != null ? m.getTags() : "";
            item.category    = "酒店";
            result.add(item);
            hotelCount++;
        }

        return result;
    }

    /**
     * 贪心最近邻算法：从起点出发，每次选最近的未访问点
     * 只有拥有真实坐标（lat/lng 均非 0）的地点才参与排序，其余附加到末尾
     */
    private List<TripItem> nearestNeighborSort(List<TripItem> items, double startLat, double startLng) {
        if (items.size() <= 1) return new ArrayList<>(items);

        // 分类：有真实坐标的景点参与排序，无坐标的商家（food/ceramic/market/hotel）单独保留
        List<TripItem> toSort = items.stream()
                .filter(i -> !"hotel".equals(i.type) && i.lat != 0 && i.lng != 0)
                .collect(Collectors.toList());
        List<TripItem> noCoord = items.stream()
                .filter(i -> !"hotel".equals(i.type) && (i.lat == 0 || i.lng == 0))
                .collect(Collectors.toList());
        List<TripItem> hotels = items.stream()
                .filter(i -> "hotel".equals(i.type))
                .collect(Collectors.toList());

        // 随机打乱候选列表，保证每次生成结果不同
        java.util.Collections.shuffle(toSort);
        java.util.Collections.shuffle(noCoord);

        List<TripItem> sorted = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        // 随机选一个起始景点，而不是每次都从地图中心出发
        double curLat = startLat, curLng = startLng;
        if (!toSort.isEmpty()) {
            int startIdx = new Random().nextInt(toSort.size());
            visited.add(startIdx);
            sorted.add(toSort.get(startIdx));
            curLat = toSort.get(startIdx).lat;
            curLng = toSort.get(startIdx).lng;
        }

        while (sorted.size() < toSort.size()) {
            int nearest = -1;
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < toSort.size(); i++) {
                if (visited.contains(i)) continue;
                double d = haversine(curLat, curLng, toSort.get(i).lat, toSort.get(i).lng);
                if (d < minDist) { minDist = d; nearest = i; }
            }
            if (nearest == -1) break;
            visited.add(nearest);
            sorted.add(toSort.get(nearest));
            curLat = toSort.get(nearest).lat;
            curLng = toSort.get(nearest).lng;
        }

        // 无坐标商家追加到排序结果后面，酒店最后
        sorted.addAll(noCoord);
        sorted.addAll(hotels);
        return sorted;
    }

    /**
     * 将排好序的地点按天分配，每天保证景点/美食/陶瓷工坊/市集均衡混排 + 1 个酒店（末尾）
     */
    private List<DayPlan> assignToDays(List<TripItem> sorted, int days, String startDate) {
        // 按类型分桶
        List<TripItem> scenics  = sorted.stream().filter(i -> "scenic".equals(i.type)).collect(Collectors.toList());
        List<TripItem> foods    = sorted.stream().filter(i -> "food".equals(i.type)).collect(Collectors.toList());
        List<TripItem> ceramics = sorted.stream().filter(i -> "ceramic".equals(i.type)).collect(Collectors.toList());
        List<TripItem> markets  = sorted.stream().filter(i -> "market".equals(i.type)).collect(Collectors.toList());
        List<TripItem> hotels   = sorted.stream().filter(i -> "hotel".equals(i.type)).collect(Collectors.toList());

        // 打乱各桶，增加随机性
        java.util.Collections.shuffle(scenics);
        java.util.Collections.shuffle(foods);
        java.util.Collections.shuffle(ceramics);
        java.util.Collections.shuffle(markets);
        java.util.Collections.shuffle(hotels);

        // 兜底：景点为空时从数据库补
        if (scenics.isEmpty()) {
            List<ScenicSpot> allScenics = scenicSpotRepository.findAll();
            for (ScenicSpot s : allScenics) {
                if (s.getLatitude() == null || s.getLongitude() == null) continue;
                TripItem item = new TripItem();
                item.type        = "scenic";
                item.id          = s.getId();
                item.name        = s.getName();
                item.lat         = s.getLatitude().doubleValue();
                item.lng         = s.getLongitude().doubleValue();
                item.image       = firstImage(s.getScenicImages(), s.getImage());
                item.description = s.getDescription() != null
                        ? s.getDescription().substring(0, Math.min(60, s.getDescription().length())) : "";
                item.address     = s.getAddress() != null ? s.getAddress() : "";
                item.price       = s.getPrice() != null ? s.getPrice().doubleValue() : 0;
                item.rating      = s.getRating() != null ? s.getRating().doubleValue() : 4.0;
                scenics.add(item);
            }
            java.util.Collections.shuffle(scenics);
        }

        // 合并陶瓷工坊和市集为"文化体验"桶
        List<TripItem> cultural = new ArrayList<>();
        cultural.addAll(ceramics);
        cultural.addAll(markets);
        java.util.Collections.shuffle(cultural);

        List<DayPlan> result = new ArrayList<>();
        int sIdx = 0, fIdx = 0, cIdx = 0, hIdx = 0;

        for (int d = 0; d < days; d++) {
            DayPlan day = new DayPlan();
            day.day  = d + 1;
            day.date = calcDate(startDate, d);

            // 每天配比：2 个景点 + 1 个文化体验 + 1 个美食（1天行程多给1个景点）
            int scenicTarget  = (days == 1) ? 3 : 2;
            int culturalTarget = 1;
            int foodTarget    = 1;

            // 景点
            for (int i = 0; i < scenicTarget; i++) {
                if (sIdx < scenics.size()) {
                    day.items.add(scenics.get(sIdx++));
                } else if (!scenics.isEmpty()) {
                    // 循环复用
                    day.items.add(scenics.get((sIdx++) % scenics.size()));
                }
            }

            // 文化体验（陶瓷工坊/市集）
            for (int i = 0; i < culturalTarget; i++) {
                if (cIdx < cultural.size()) {
                    day.items.add(cultural.get(cIdx++));
                }
                // 没有文化体验时不强制补，避免重复
            }

            // 美食
            for (int i = 0; i < foodTarget; i++) {
                if (fIdx < foods.size()) {
                    day.items.add(foods.get(fIdx++));
                }
            }

            // 多天行程每天末尾加酒店
            if (days > 1 && !hotels.isEmpty()) {
                int hotelIdx = d < hotels.size() ? d : (d % hotels.size());
                day.items.add(hotels.get(hotelIdx));
            }

            // 打乱当天顺序（酒店保持最后）
            TripItem hotel = day.items.stream().filter(i -> "hotel".equals(i.type)).findFirst().orElse(null);
            List<TripItem> nonHotelItems = day.items.stream().filter(i -> !"hotel".equals(i.type)).collect(Collectors.toList());
            java.util.Collections.shuffle(nonHotelItems);
            day.items = new ArrayList<>(nonHotelItems);
            if (hotel != null) day.items.add(hotel);

            day.note = buildDayNote(d, days, day.items);
            result.add(day);
        }

        return result;
    }

    // ── 工具方法 ──────────────────────────────────────────────

    /** 根据当天行程内容生成备注 */
    private String buildDayNote(int dayIndex, int totalDays, List<TripItem> items) {
        long scenicCount  = items.stream().filter(i -> "scenic".equals(i.type)).count();
        long foodCount    = items.stream().filter(i -> "food".equals(i.type)).count();
        long ceramicCount = items.stream().filter(i -> "ceramic".equals(i.type) || "market".equals(i.type)).count();
        boolean hasHotel  = items.stream().anyMatch(i -> "hotel".equals(i.type));

        StringBuilder sb = new StringBuilder();
        if (dayIndex == 0) sb.append("出发日，");
        else if (dayIndex == totalDays - 1 && totalDays > 1) sb.append("返程日，");

        if (scenicCount > 0) sb.append("游览 ").append(scenicCount).append(" 处景点");
        if (ceramicCount > 0) {
            if (sb.length() > 0 && !sb.toString().endsWith("，")) sb.append("，");
            sb.append("体验陶瓷文化");
        }
        if (foodCount > 0) {
            if (sb.length() > 0 && !sb.toString().endsWith("，")) sb.append("，");
            sb.append("品尝当地美食");
        }
        if (hasHotel) {
            if (sb.length() > 0 && !sb.toString().endsWith("，")) sb.append("，");
            sb.append("入住酒店休息");
        }
        if (sb.length() == 0) sb.append("自由活动，探索景德镇");
        return sb.toString();
    }

    /** Haversine 距离（km） */
    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** 取第一张图片 URL */
    private String firstImage(String multi, String single) {
        if (multi != null && !multi.isBlank()) {
            String[] parts = multi.split(",");
            if (parts.length > 0 && !parts[0].isBlank()) return parts[0].trim();
        }
        return single != null ? single : "";
    }

    /** 根据出发日期偏移 d 天 */
    private String calcDate(String startDate, int offset) {
        if (startDate == null || startDate.isBlank()) return "";
        try {
            java.time.LocalDate base = java.time.LocalDate.parse(startDate);
            return base.plusDays(offset).toString();
        } catch (Exception e) {
            return "";
        }
    }
}
