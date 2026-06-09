package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Favorite;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jdz.tourism.annotation.LogOperation;

@RestController
@RequestMapping("/api/scenic")
@CrossOrigin(origins = "*")
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    /**
     * 添加收藏
     */
    @LogOperation("添加收藏")
    @PostMapping("/favorite")
    public ResponseEntity<Map<String, Object>> addFavorite(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(requestData.get("userId").toString());
            Long scenicId = Long.valueOf(requestData.get("scenicId").toString());
            
            Favorite favorite = favoriteService.addFavorite(userId, scenicId);
            response.put("success", true);
            response.put("message", "收藏成功");
            response.put("data", favorite);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 取消收藏
     */
    @LogOperation("取消收藏")
    @DeleteMapping("/favorite/{scenicId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable Long scenicId, 
            @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            favoriteService.removeFavorite(userId, scenicId);
            response.put("success", true);
            response.put("message", "取消收藏成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取用户收藏列表
     */
    @GetMapping("/favorites")
    public ResponseEntity<Map<String, Object>> getUserFavorites(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScenicSpot> favorites = favoriteService.getUserFavoriteScenics(userId);
            response.put("success", true);
            response.put("data", favorites);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/favorite/{scenicId}")
    public ResponseEntity<Map<String, Object>> checkFavorite(
            @PathVariable Long scenicId, 
            @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isFavorited = favoriteService.isFavorited(userId, scenicId);
            response.put("success", true);
            response.put("data", isFavorited);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}