package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.service.MerchantDeleteJobService;
import com.jdz.tourism.service.MerchantService;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/merchant")
@CrossOrigin(origins = "*")
public class MerchantController {
    
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantDeleteJobService merchantDeleteJobService;
    
    /**
     * 创建商家
     */
    @LogOperation("创建商家")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMerchant(@RequestBody Map<String, Object> merchantData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(merchantData.get("userId").toString());
            String shopName = merchantData.get("shopName").toString();
            String description = merchantData.get("description") != null ? merchantData.get("description").toString() : "";
            String category = merchantData.get("category") != null ? merchantData.get("category").toString() : "OTHER";
            
            Merchant merchant = merchantService.createMerchant(userId, shopName, description, category);
            response.put("success", true);
            response.put("message", "商家创建成功");
            response.put("data", merchant);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据景点ID获取关联的商家
     * 专门用于景点类型的商家查询
     * 注意：这个路由必须在 /{id} 之前定义，以确保正确匹配
     */
    @GetMapping("/scenic/{scenicId}")
    public ResponseEntity<Map<String, Object>> getMerchantByScenicId(@PathVariable Long scenicId) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("========== 收到请求: /api/merchant/scenic/" + scenicId + " ==========");
        try {
            System.out.println("查询景点ID对应的商家: " + scenicId);
            Optional<Merchant> merchantOpt = merchantService.getMerchantByScenicSpotId(scenicId);
            System.out.println("查询结果: " + (merchantOpt.isPresent() ? "找到商家" : "未找到商家"));
            
            if (merchantOpt.isPresent()) {
                Merchant merchant = merchantOpt.get();
                System.out.println("找到商家: ID=" + merchant.getId() + ", shopName=" + merchant.getShopName() + ", category=" + merchant.getCategory());
                Map<String, Object> merchantData = merchantService.enrichMerchantWithRating(merchant);
                response.put("success", true);
                response.put("data", merchantData);
                System.out.println("========== 返回成功响应 ==========");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("未找到景点ID " + scenicId + " 对应的商家");
                response.put("success", true);
                response.put("message", "该景点没有关联的商家");
                response.put("data", null);
                System.out.println("========== 返回200 (无关联商家) ==========");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            System.err.println("========== 查询商家时发生错误 ==========");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("错误类型: " + e.getClass().getName());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据ID获取商家详情
     * 支持商家ID和景点ID（如果是景点类型的商家）
     * 注意：这个路由必须在 /scenic/{scenicId} 之后定义
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMerchantById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("查询商家ID: " + id);
            Optional<Merchant> merchantOpt = merchantService.getMerchantById(id);
            if (merchantOpt.isPresent()) {
                System.out.println("找到商家: " + merchantOpt.get().getId() + ", " + merchantOpt.get().getShopName());
                // 使用带评分的商家信息，方便前端直接展示详情
                Map<String, Object> merchantData = merchantService.enrichMerchantWithRating(merchantOpt.get());
                response.put("success", true);
                response.put("data", merchantData);
                return ResponseEntity.ok(response);
            } else {
                System.out.println("未找到商家ID: " + id);
                response.put("success", false);
                response.put("message", "商家不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("查询商家时发生错误: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据用户ID获取商家
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getMerchantByUserId(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("========== 正在查询用户ID " + userId + " 的商家信息 ==========");
            Optional<Merchant> merchant = merchantService.getMerchantByUserId(userId);
            if (merchant.isPresent()) {
                Merchant m = merchant.get();
                System.out.println("找到商家: ID=" + m.getId() + ", shopName=" + m.getShopName());
                System.out.println("商家图片: avatar=" + m.getAvatar() + ", shopImages=" + m.getShopImages());
                
                // 使用enrichMerchantWithRating获取包含评分和评论数的完整商家信息
                Map<String, Object> merchantData = merchantService.enrichMerchantWithRating(m);
                
                response.put("success", true);
                response.put("data", merchantData);
                return ResponseEntity.ok(response);
            } else {
                System.out.println("未找到用户ID " + userId + " 的商家信息");
                response.put("success", false);
                response.put("message", "该用户不是商家");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("查询用户商家信息失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据店铺名称获取商家
     */
    @GetMapping("/shop/{shopName}")
    public ResponseEntity<Map<String, Object>> getMerchantByShopName(@PathVariable String shopName) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Merchant> merchant = merchantService.getMerchantByShopName(shopName);
            if (merchant.isPresent()) {
                response.put("success", true);
                response.put("data", merchant.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "商家不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新商家信息
     */
    @LogOperation("更新商家信息")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMerchant(@PathVariable Long id, @Valid @RequestBody Merchant merchant) {
        Map<String, Object> response = new HashMap<>();
        try {
            merchant.setId(id);
            Merchant updatedMerchant = merchantService.updateMerchant(merchant);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", updatedMerchant);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新商家基础信息（店铺名称、描述、地址、电话、营业时间、头像、图片集合）
     * 供商家端“店铺管理”页面使用，避免覆盖关联关系等字段
     */
    @LogOperation("更新商家基础信息")
    @PutMapping("/{id}/basic-info")
    public ResponseEntity<Map<String, Object>> updateMerchantBasicInfo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> basicInfo) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Merchant> merchantOpt = merchantService.getMerchantById(id);
            if (merchantOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "商家不存在");
                return ResponseEntity.notFound().build();
            }
            
            Merchant merchant = merchantOpt.get();
            
            if (basicInfo.containsKey("shopName")) {
                merchant.setShopName(basicInfo.get("shopName") != null ? basicInfo.get("shopName").toString() : merchant.getShopName());
            }
            if (basicInfo.containsKey("description")) {
                merchant.setDescription(basicInfo.get("description") != null ? basicInfo.get("description").toString() : null);
            }
            if (basicInfo.containsKey("address")) {
                merchant.setAddress(basicInfo.get("address") != null ? basicInfo.get("address").toString() : null);
            }
            if (basicInfo.containsKey("phone")) {
                merchant.setPhone(basicInfo.get("phone") != null ? basicInfo.get("phone").toString() : null);
            }
            if (basicInfo.containsKey("openTime")) {
                merchant.setOpenTime(basicInfo.get("openTime") != null ? basicInfo.get("openTime").toString() : null);
            }
            if (basicInfo.containsKey("startPrice")) {
                Object val = basicInfo.get("startPrice");
                if (val != null && !val.toString().isEmpty()) {
                    try {
                        merchant.setStartPrice(new java.math.BigDecimal(val.toString()));
                    } catch (NumberFormatException e) {
                        // ignore invalid number
                    }
                } else {
                    merchant.setStartPrice(null);
                }
            }
            if (basicInfo.containsKey("adminRating")) {
                Object val = basicInfo.get("adminRating");
                if (val != null && !val.toString().isEmpty()) {
                    try {
                        merchant.setAdminRating(Double.valueOf(val.toString()));
                    } catch (NumberFormatException e) {
                        // ignore invalid number
                    }
                } else {
                    merchant.setAdminRating(null);
                }
            }
            if (basicInfo.containsKey("avatar")) {
                merchant.setAvatar(basicInfo.get("avatar") != null ? basicInfo.get("avatar").toString() : null);
            }
            // 多张店铺图片（用于酒店详情轮播），前端以数组形式传递
            if (basicInfo.containsKey("images")) {
                Object imagesObj = basicInfo.get("images");
                if (imagesObj instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) imagesObj;
                    String joined = list.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .map(this::normalizeRelativeUrl)
                            .distinct()
                            .collect(Collectors.joining(","));
                    merchant.setShopImages(joined.isEmpty() ? null : joined);
                } else if (imagesObj != null) {
                    // 兼容直接传字符串（可能是逗号分隔或JSON数组字符串）
                    String raw = imagesObj.toString().trim();
                    String normalized = normalizeImagesRaw(raw);
                    merchant.setShopImages(normalized);
                }
            }
            // 标签（数组 → 逗号分隔字符串）
            if (basicInfo.containsKey("tags")) {
                Object tagsObj = basicInfo.get("tags");
                if (tagsObj instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) tagsObj;
                    String joined = list.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .collect(Collectors.joining(","));
                    merchant.setTags(joined.isEmpty() ? null : joined);
                } else if (tagsObj != null) {
                    merchant.setTags(tagsObj.toString().trim().isEmpty() ? null : tagsObj.toString().trim());
                } else {
                    merchant.setTags(null);
                }
            }
            // 设施（数组 → 逗号分隔字符串）
            if (basicInfo.containsKey("facilities")) {
                Object facObj = basicInfo.get("facilities");
                if (facObj instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) facObj;
                    String joined = list.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .collect(Collectors.joining(","));
                    merchant.setFacilities(joined.isEmpty() ? null : joined);
                } else if (facObj != null) {
                    merchant.setFacilities(facObj.toString().trim().isEmpty() ? null : facObj.toString().trim());
                } else {
                    merchant.setFacilities(null);
                }
            }
            // 启用状态
            if (basicInfo.containsKey("enabled")) {
                Object val = basicInfo.get("enabled");
                if (val != null) {
                    merchant.setEnabled(Boolean.parseBoolean(val.toString()));
                }
            }
            // 美食商家：人均消费
            if (basicInfo.containsKey("avgPrice")) {
                Object val = basicInfo.get("avgPrice");
                merchant.setAvgPrice(val != null && !val.toString().isEmpty() ? val.toString() : null);
            }
            // 美食商家：餐饮类型
            if (basicInfo.containsKey("cuisineType")) {
                Object val = basicInfo.get("cuisineType");
                merchant.setCuisineType(val != null && !val.toString().isEmpty() ? val.toString() : null);
            }
            // 酒店商家：星级
            if (basicInfo.containsKey("starRating")) {
                Object val = basicInfo.get("starRating");
                merchant.setStarRating(val != null && !val.toString().isEmpty() ? val.toString() : null);
            }
            // 酒店商家：房间数
            if (basicInfo.containsKey("roomCount")) {
                Object val = basicInfo.get("roomCount");
                if (val != null && !val.toString().isEmpty()) {
                    try {
                        merchant.setRoomCount(Integer.parseInt(val.toString()));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else {
                    merchant.setRoomCount(null);
                }
            }
            
            Merchant updated = merchantService.updateMerchant(merchant);
            Map<String, Object> data = merchantService.enrichMerchantWithRating(updated);
            
            response.put("success", true);
            response.put("message", "基础信息更新成功");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // --- helpers for image sanitize ---
    private String normalizeRelativeUrl(String url) {
        if (url == null) return null;
        String u = url.trim();
        if (u.isEmpty()) return null;
        if (u.startsWith("http") || u.startsWith("data:image/")) return u;
        if (u.startsWith("/")) return u;
        return "/" + u;
    }

    private String normalizeImagesRaw(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim();
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        try {
            if (s.startsWith("[") && s.endsWith("]")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<String> arr = mapper.readValue(s, mapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                for (String it : arr) {
                    if (it != null && !it.trim().isEmpty()) set.add(normalizeRelativeUrl(it));
                }
            } else {
                for (String it : s.split(",")) {
                    if (it != null && !it.trim().isEmpty()) set.add(normalizeRelativeUrl(it));
                }
            }
        } catch (Exception ignore) {
            for (String it : s.split(",")) {
                if (it != null && !it.trim().isEmpty()) set.add(normalizeRelativeUrl(it));
            }
        }
        return String.join(",", set);
    }
    
    /**
     * 获取所有商家
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMerchants() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> merchants = merchantService.getAllMerchants().stream()
                    .map(merchantService::enrichMerchantWithRating)
                    .collect(java.util.stream.Collectors.toList());
            response.put("success", true);
            response.put("data", merchants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除商家
     */
    @LogOperation("删除商家")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMerchant(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            merchantService.deleteMerchant(id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建异步删除商家任务（接口秒回）
     */
    @LogOperation("创建异步删除商家任务")
    @PostMapping("/{id}/delete-jobs")
    public ResponseEntity<Map<String, Object>> createDeleteMerchantJob(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            MerchantDeleteJobService.MerchantDeleteJob job = merchantDeleteJobService.createDeleteJob(id);
            Map<String, Object> data = new HashMap<>();
            data.put("jobId", job.getJobId());
            data.put("merchantId", job.getMerchantId());
            data.put("status", job.getStatus().name());
            data.put("phase", job.getPhase());
            data.put("createdAt", job.getCreatedAt());

            response.put("success", true);
            response.put("message", "删除任务已创建");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 查询异步删除商家任务状态
     */
    @GetMapping("/delete-jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getDeleteMerchantJobStatus(@PathVariable String jobId) {
        Map<String, Object> response = new HashMap<>();
        Optional<MerchantDeleteJobService.MerchantDeleteJob> jobOpt = merchantDeleteJobService.getJob(jobId);
        if (jobOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "删除任务不存在");
            return ResponseEntity.notFound().build();
        }

        MerchantDeleteJobService.MerchantDeleteJob job = jobOpt.get();
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", job.getJobId());
        data.put("merchantId", job.getMerchantId());
        data.put("status", job.getStatus().name());
        data.put("phase", job.getPhase());
        data.put("errorMessage", job.getErrorMessage());
        data.put("createdAt", job.getCreatedAt());
        data.put("startedAt", job.getStartedAt());
        data.put("completedAt", job.getCompletedAt());

        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据分类获取商家列表（个性化推荐）
     * userId 有值时走 Jaccard 标签匹配，无值时按评分降序（冷启动）
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getMerchantsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> enrichedMerchants = merchantService.recommendMerchants(category, userId);
            response.put("success", true);
            response.put("data", enrichedMerchants);
            response.put("total", enrichedMerchants.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}