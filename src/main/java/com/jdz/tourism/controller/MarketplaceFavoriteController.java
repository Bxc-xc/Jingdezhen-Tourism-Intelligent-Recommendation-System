package com.jdz.tourism.controller;

import com.jdz.tourism.annotation.LogOperation;
import com.jdz.tourism.service.MarketplaceFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marketplace/favorite")
@CrossOrigin(origins = "*")
public class MarketplaceFavoriteController {

    @Autowired
    private MarketplaceFavoriteService marketplaceFavoriteService;

    @LogOperation("添加市集收藏")
    @PostMapping
    public ResponseEntity<Map<String, Object>> addFavorite(@RequestBody Map<String, Object> req) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = Long.valueOf(req.get("userId").toString());
            Long marketplaceId = Long.valueOf(req.get("marketplaceId").toString());
            marketplaceFavoriteService.addFavorite(userId, marketplaceId);
            res.put("success", true);
            res.put("message", "收藏成功");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @LogOperation("取消市集收藏")
    @DeleteMapping("/{marketplaceId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable Long marketplaceId,
            @RequestParam Long userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            marketplaceFavoriteService.removeFavorite(userId, marketplaceId);
            res.put("success", true);
            res.put("message", "取消收藏成功");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/{marketplaceId}")
    public ResponseEntity<Map<String, Object>> check(
            @PathVariable Long marketplaceId,
            @RequestParam Long userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            boolean favorited = marketplaceFavoriteService.isFavorited(userId, marketplaceId);
            res.put("success", true);
            res.put("data", favorited);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam Long userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            List<Map<String, Object>> data = marketplaceFavoriteService.getUserFavorites(userId).stream().map(f -> {
                com.jdz.tourism.entity.Marketplace m = f.getMarketplace();
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("id", m.getId());
                item.put("name", m.getName());
                item.put("coverImage", m.getCoverImage());
                item.put("carouselImages", m.getCarouselImages());
                item.put("description", m.getDescription());
                item.put("address", m.getAddress());
                item.put("openTime", m.getOpenTime());
                item.put("createTime", f.getCreateTime().toString());
                return item;
            }).collect(java.util.stream.Collectors.toList());
            res.put("success", true);
            res.put("data", data);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}
