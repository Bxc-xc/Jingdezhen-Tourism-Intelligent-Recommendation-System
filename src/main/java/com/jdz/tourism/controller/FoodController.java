package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Food;
import com.jdz.tourism.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jdz.tourism.annotation.LogOperation;

@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "*")
@Tag(name = "美食管理", description = "美食推荐相关的API接口")
public class FoodController {
    
    @Autowired
    private FoodService foodService;
    
    /**
     * 获取所有美食推荐
     */
    @Operation(summary = "获取美食推荐列表", description = "获取所有美食推荐信息")
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendFoods(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "true") Boolean includeMerchants,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 有 userId 时走个性化推荐（直接从商家推荐接口获取）
            if (userId != null && (category == null || category.isEmpty()) && (keyword == null || keyword.isEmpty())) {
                List<Map<String, Object>> recommended = foodService.recommendFoodMerchants(userId);
                response.put("success", true);
                response.put("data", recommended);
                response.put("total", recommended.size());
                return ResponseEntity.ok(response);
            }

            List<Food> foods;
            if (category != null && !category.isEmpty()) {
                foods = foodService.getFoodsByCategory(category);
            } else if (keyword != null && !keyword.isEmpty()) {
                foods = foodService.searchFoods(keyword);
            } else {
                foods = foodService.getAllFoods();
            }

            List<Object> allFoods = new ArrayList<>(foods);
            if (includeMerchants) {
                List<Map<String, Object>> merchantFoods = foodService.getFoodsFromMerchants();
                allFoods.addAll(merchantFoods);
            }

            response.put("success", true);
            response.put("data", allFoods);
            response.put("total", allFoods.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取美食详情
     */
    @Operation(summary = "获取美食详情", description = "根据ID获取美食详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFoodDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            return foodService.getFoodById(id)
                    .map(food -> {
                        response.put("success", true);
                        response.put("data", food);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除美食
     */
    @Operation(summary = "删除美食", description = "根据ID删除美食信息")
    @LogOperation("删除美食")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFood(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            foodService.deleteFood(id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

