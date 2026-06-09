package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Marketplace;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.TravelRoute;
import com.jdz.tourism.repository.MarketplaceRepository;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.TravelRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局模糊搜索接口
 * 覆盖：景点、陶瓷市集、陶瓷工坊、餐饮、酒店、路线
 */
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MarketplaceRepository marketplaceRepository;

    @Autowired
    private TravelRouteRepository travelRouteRepository;

    /**
     * 全局搜索
     * GET /api/search?keyword=xxx
     * 返回结构：{ success, data: { scenic[], food[], hotel[], ceramic[], marketplace[], route[] }, total }
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false, defaultValue = "") String keyword) {

        Map<String, Object> response = new HashMap<>();
        try {
            String kw = keyword.trim().toLowerCase();

            // 搜索商家（景点/餐饮/酒店/陶瓷工坊）
            List<Merchant> allMerchants = merchantRepository.findAll().stream()
                    .filter(m -> !Boolean.TRUE.equals(m.getDeleted()) && Boolean.TRUE.equals(m.getEnabled()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> scenic = new ArrayList<>();
            List<Map<String, Object>> food = new ArrayList<>();
            List<Map<String, Object>> hotel = new ArrayList<>();
            List<Map<String, Object>> ceramic = new ArrayList<>();

            for (Merchant m : allMerchants) {
                if (!matchesMerchant(m, kw)) continue;
                Map<String, Object> item = merchantToMap(m);
                String cat = m.getCategory() != null ? m.getCategory().toUpperCase() : "";
                switch (cat) {
                    case "SCENIC" -> scenic.add(item);
                    case "FOOD"   -> food.add(item);
                    case "HOTEL"  -> hotel.add(item);
                    case "CERAMIC" -> ceramic.add(item);
                    default -> food.add(item); // 其他归入餐饮
                }
            }

            // 搜索陶瓷市集
            List<Map<String, Object>> marketplace = marketplaceRepository
                    .findAllByEnabledTrueOrderBySortOrderAscIdAsc().stream()
                    .filter(mp -> matchesMarketplace(mp, kw))
                    .map(this::marketplaceToMap)
                    .collect(Collectors.toList());

            // 搜索路线
            List<Map<String, Object>> route = travelRouteRepository.findAll().stream()
                    .filter(r -> matchesRoute(r, kw))
                    .map(this::routeToMap)
                    .collect(Collectors.toList());

            int total = scenic.size() + food.size() + hotel.size()
                    + ceramic.size() + marketplace.size() + route.size();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("scenic", scenic);
            data.put("food", food);
            data.put("hotel", hotel);
            data.put("ceramic", ceramic);
            data.put("marketplace", marketplace);
            data.put("route", route);

            response.put("success", true);
            response.put("data", data);
            response.put("total", total);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ---- 匹配逻辑 ----

    private boolean matchesMerchant(Merchant m, String kw) {
        if (kw.isEmpty()) return true;
        String text = concat(m.getShopName(), m.getDescription(), m.getAddress());
        return text.contains(kw);
    }

    private boolean matchesMarketplace(Marketplace mp, String kw) {
        if (kw.isEmpty()) return true;
        String text = concat(mp.getName(), mp.getDescription(), mp.getAddress());
        return text.contains(kw);
    }

    private boolean matchesRoute(TravelRoute r, String kw) {
        if (kw.isEmpty()) return true;
        String text = concat(r.getName(), r.getDescription(), r.getTags());
        return text.contains(kw);
    }

    private String concat(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null) sb.append(p.toLowerCase());
        }
        return sb.toString();
    }

    // ---- 序列化 ----

    private Map<String, Object> merchantToMap(Merchant m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getShopName());
        map.put("description", m.getDescription());
        map.put("address", m.getAddress());
        map.put("price", m.getStartPrice());
        map.put("rating", m.getAdminRating());
        map.put("category", m.getCategory());
        map.put("type", m.getCategory());
        // 封面图：优先 shopImages 第一张，其次 avatar
        String cover = "";
        if (m.getShopImages() != null && !m.getShopImages().isBlank()) {
            String[] imgs = m.getShopImages().split(",");
            if (imgs.length > 0) cover = imgs[0].trim();
        }
        if (cover.isEmpty() && m.getAvatar() != null) cover = m.getAvatar();
        map.put("image", cover);
        // 景点关联ID
        if (m.getScenicSpot() != null) map.put("scenicSpotId", m.getScenicSpot().getId());
        return map;
    }

    private Map<String, Object> marketplaceToMap(Marketplace mp) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", mp.getId());
        map.put("name", mp.getName());
        map.put("description", mp.getDescription());
        map.put("address", mp.getAddress());
        map.put("type", "MARKETPLACE");
        map.put("category", "MARKETPLACE");
        // 封面图
        String cover = mp.getCoverImage();
        if ((cover == null || cover.isBlank()) && mp.getCarouselImages() != null) {
            String[] imgs = mp.getCarouselImages().split(",");
            if (imgs.length > 0) cover = imgs[0].trim();
        }
        map.put("image", cover != null ? cover : "");
        return map;
    }

    private Map<String, Object> routeToMap(TravelRoute r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("name", r.getName());
        map.put("description", r.getDescription());
        map.put("image", r.getImage() != null ? r.getImage() : "");
        map.put("price", r.getTotalPrice());
        map.put("tags", r.getTags());
        map.put("days", r.getDays());
        map.put("difficulty", r.getDifficulty());
        map.put("type", "ROUTE");
        map.put("category", "ROUTE");
        return map;
    }
}
