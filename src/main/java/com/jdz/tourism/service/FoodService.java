package com.jdz.tourism.service;

import com.jdz.tourism.entity.Food;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FoodService {
    
    @Autowired
    private FoodRepository foodRepository;
    
    @Autowired
    private MerchantService merchantService;
    
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }
    
    public Optional<Food> getFoodById(Long id) {
        return foodRepository.findById(id);
    }
    
    public List<Food> getFoodsByCategory(String category) {
        return foodRepository.findByCategory(category);
    }
    
    public List<Food> searchFoods(String keyword) {
        return foodRepository.findByNameContaining(keyword);
    }
    
    public Food saveFood(Food food) {
        return foodRepository.save(food);
    }
    
    public void deleteFood(Long id) {
        foodRepository.deleteById(id);
    }
    
    /**
     * 从商家获取美食数据（将美食商家转换为Food格式）
     * 将商家在店铺管理中保存的信息映射到游客端美食推荐卡片
     */
    public List<Map<String, Object>> getFoodsFromMerchants() {
        List<Merchant> foodMerchants = merchantService.getMerchantsByCategory("FOOD");
        List<Map<String, Object>> foodList = new ArrayList<>();
        
        for (Merchant merchant : foodMerchants) {
            // 使用enrichMerchantWithRating获取包含评分和评论数的完整商家信息
            Map<String, Object> enrichedMerchant = merchantService.enrichMerchantWithRating(merchant);
            
            Map<String, Object> foodMap = new HashMap<>();
            // 基本信息映射：商家保存的店铺名称 -> 美食卡片名称
            foodMap.put("id", merchant.getId());
            foodMap.put("name", merchant.getShopName() != null ? merchant.getShopName() : "");
            // 商家保存的描述 -> 美食卡片描述
            foodMap.put("description", merchant.getDescription() != null ? merchant.getDescription() : "");
            foodMap.put("category", "美食商家");
            // 商家保存的地址 -> 美食卡片地址
            foodMap.put("address", merchant.getAddress() != null ? merchant.getAddress() : "");
            
            // 评分和评论数：从评价系统获取（使用真实评分数据，如果没有评分则返回null或0.0）
            Object ratingObj = enrichedMerchant.get("rating");
            if (ratingObj != null) {
                foodMap.put("rating", ratingObj);
            } else {
                // 如果没有评分，返回0.0而不是写死的默认值
                foodMap.put("rating", 0.0);
            }
            foodMap.put("reviewCount", enrichedMerchant.get("reviewCount") != null ? enrichedMerchant.get("reviewCount") : 0);
            
            // 资质审核状态
            foodMap.put("qualificationApproved", enrichedMerchant.get("qualificationApproved"));
            
            // 价格：优先使用商家的avgPrice（人均消费字符串，如"50-100"），其次使用startPrice，否则使用默认值
            if (merchant.getAvgPrice() != null && !merchant.getAvgPrice().isEmpty()) {
                // avgPrice是字符串格式，如"50-100"，前端可以直接显示
                foodMap.put("avgPrice", merchant.getAvgPrice());
                // 同时保留price字段用于排序和筛选（尝试解析avgPrice的第一个数字）
                try {
                    String[] priceRange = merchant.getAvgPrice().split("-");
                    if (priceRange.length > 0) {
                        foodMap.put("price", Double.parseDouble(priceRange[0].trim()));
                    } else {
                        foodMap.put("price", 50);
                    }
                } catch (Exception e) {
                    foodMap.put("price", 50);
                }
            } else if (merchant.getStartPrice() != null) {
                foodMap.put("price", merchant.getStartPrice());
            } else {
                foodMap.put("price", 50); // 默认人均价格
            }
            
            // 餐饮类型：商家在店铺管理中保存的cuisineType -> 美食卡片的category字段
            if (merchant.getCuisineType() != null && !merchant.getCuisineType().isEmpty()) {
                foodMap.put("category", merchant.getCuisineType());
            } else {
                foodMap.put("category", "美食商家");
            }
            
            // 图片：优先使用avatar（封面图），如果没有则使用shopImages的第一张
            String imageUrl = "";
            if (merchant.getAvatar() != null && !merchant.getAvatar().isEmpty()) {
                imageUrl = merchant.getAvatar();
            } else if (merchant.getShopImages() != null && !merchant.getShopImages().isEmpty()) {
                // shopImages是逗号分隔的字符串，取第一张
                String[] images = merchant.getShopImages().split(",");
                if (images.length > 0 && !images[0].trim().isEmpty()) {
                    imageUrl = images[0].trim();
                }
            }
            foodMap.put("image", imageUrl);
            
            // 标签：根据商家信息生成标签数组
            List<String> tags = new ArrayList<>();
            tags.add("商家");
            tags.add("美食");
            // 可以根据商家分类或其他信息添加更多标签
            if (merchant.getAddress() != null && !merchant.getAddress().isEmpty()) {
                // 可以从地址中提取区域信息作为标签
                // 这里暂时不添加，保持简洁
            }
            foodMap.put("tags", tags); // 前端期望的是数组格式
            
            // 联系方式和营业时间
            foodMap.put("phone", merchant.getPhone());
            foodMap.put("openTime", merchant.getOpenTime());

            // 管理员评分
            foodMap.put("adminRating", merchant.getAdminRating());

            // 餐饮类型（冗余保留，供编辑表单使用）
            foodMap.put("cuisineType", merchant.getCuisineType());

            // 人均消费（冗余保留）
            foodMap.put("avgPrice", merchant.getAvgPrice() != null ? merchant.getAvgPrice() : "");

            // 多图（供编辑表单回显）
            foodMap.put("shopImages", enrichedMerchant.get("shopImages"));
            foodMap.put("images", enrichedMerchant.get("images"));

            // 标记为商家数据，方便前端识别
            foodMap.put("merchantId", merchant.getId());
            foodMap.put("isMerchant", true);
            
            // 补充用户信息（解决前端关联账号显示问题）
            if (merchant.getUser() != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", merchant.getUser().getId());
                userMap.put("username", merchant.getUser().getUsername());
                foodMap.put("user", userMap);
            }
            
            // 补充活动信息
            if (enrichedMerchant.containsKey("activities")) {
                foodMap.put("activities", enrichedMerchant.get("activities"));
            }
            
            foodList.add(foodMap);
        }
        
        return foodList;
    }

    /**
     * 个性化美食推荐（复用 MerchantService 的推荐算法）
     */
    public List<Map<String, Object>> recommendFoodMerchants(Long userId) {
        return merchantService.recommendMerchants("FOOD", userId);
    }
}

