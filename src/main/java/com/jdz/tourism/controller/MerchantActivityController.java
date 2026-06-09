package com.jdz.tourism.controller;

import com.jdz.tourism.entity.MerchantActivity;
import com.jdz.tourism.service.MerchantActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jdz.tourism.annotation.LogOperation;
import com.jdz.tourism.repository.MerchantActivityRepository;
import com.jdz.tourism.repository.MerchantRepository;

@RestController
@RequestMapping("/api/merchant/activity")
@CrossOrigin(origins = "*")
public class MerchantActivityController {
    
    @Autowired
    private MerchantActivityService activityService;

    @Autowired
    private MerchantActivityRepository activityRepository;

    @Autowired
    private MerchantRepository merchantRepository;
    
    /**
     * 获取商家的所有活动
     */
    @GetMapping("/list/{merchantId}")
    public ResponseEntity<Map<String, Object>> getActivities(@PathVariable Long merchantId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<MerchantActivity> activities = activityService.getActivitiesByMerchantId(merchantId);
            response.put("success", true);
            response.put("data", activities);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 创建活动
     */
    @LogOperation("发布商家活动")
    @PostMapping("/{merchantId}")
    public ResponseEntity<Map<String, Object>> createActivity(
            @PathVariable Long merchantId, 
            @RequestBody Map<String, Object> activityData) {
        Map<String, Object> response = new HashMap<>();
        try {
            MerchantActivity activity = new MerchantActivity();
            activity.setTitle(activityData.get("title").toString());
            activity.setDescription(activityData.get("description").toString());
            activity.setType(activityData.get("type").toString());
            // 图片与折扣（可选）
            if (activityData.containsKey("image") && activityData.get("image") != null) {
                activity.setImage(activityData.get("image").toString());
            }
            if (activityData.containsKey("discount") && activityData.get("discount") != null) {
                try {
                    activity.setDiscount(Double.valueOf(activityData.get("discount").toString()));
                } catch (NumberFormatException ignore) { }
            }
            
            // 解析时间字符串，格式通常为 "yyyy-MM-dd HH:mm"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            // 前端可能传过来的是 "yyyy-MM-dd HH:mm" 或者 ISO 格式，这里假设是 Element Plus 的默认格式
            // 如果是数组传参需要特别处理，但这里假设前端已经处理好
            
            String startStr = activityData.get("startTime").toString();
            String endStr = activityData.get("endTime").toString();
            
            // 处理可能的 "T" 或秒数
            if (startStr.contains("T")) startStr = startStr.replace("T", " ").substring(0, 16);
            if (endStr.contains("T")) endStr = endStr.replace("T", " ").substring(0, 16);
            if (startStr.length() > 16) startStr = startStr.substring(0, 16);
            if (endStr.length() > 16) endStr = endStr.substring(0, 16);
            
            activity.setStartTime(LocalDateTime.parse(startStr, formatter));
            activity.setEndTime(LocalDateTime.parse(endStr, formatter));
            
            MerchantActivity created = activityService.createActivity(merchantId, activity);
            
            response.put("success", true);
            response.put("message", "活动发布成功");
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "发布失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新活动
     */
    @LogOperation("更新商家活动")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateActivity(
            @PathVariable Long id,
            @RequestBody Map<String, Object> activityData) {
        Map<String, Object> response = new HashMap<>();
        try {
            MerchantActivity activity = new MerchantActivity();
            if (activityData.containsKey("title")) {
                activity.setTitle(activityData.get("title").toString());
            }
            if (activityData.containsKey("description")) {
                activity.setDescription(activityData.get("description").toString());
            }
            if (activityData.containsKey("type")) {
                activity.setType(activityData.get("type").toString());
            }
            if (activityData.containsKey("image")) {
                Object v = activityData.get("image");
                activity.setImage(v == null ? null : v.toString());
            }
            if (activityData.containsKey("discount")) {
                Object v = activityData.get("discount");
                if (v == null || v.toString().isBlank()) {
                    activity.setDiscount(null);
                } else {
                    try {
                        activity.setDiscount(Double.valueOf(v.toString()));
                    } catch (NumberFormatException ignore) { }
                }
            }
            
            // 解析时间
            if (activityData.containsKey("startTime")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String startStr = activityData.get("startTime").toString();
                if (startStr.contains("T")) startStr = startStr.replace("T", " ").substring(0, 16);
                if (startStr.length() > 16) startStr = startStr.substring(0, 16);
                activity.setStartTime(LocalDateTime.parse(startStr, formatter));
            }
            if (activityData.containsKey("endTime")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String endStr = activityData.get("endTime").toString();
                if (endStr.contains("T")) endStr = endStr.replace("T", " ").substring(0, 16);
                if (endStr.length() > 16) endStr = endStr.substring(0, 16);
                activity.setEndTime(LocalDateTime.parse(endStr, formatter));
            }
            
            MerchantActivity updated = activityService.updateActivity(id, activity);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除活动
     */
    @LogOperation("删除商家活动")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteActivity(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            activityService.deleteActivity(id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========== 管理员接口 ==========

    /**
     * 管理员：获取所有商家的活动列表
     */
    @LogOperation("管理员查看所有店铺活动")
    @GetMapping("/admin/all")
    public ResponseEntity<Map<String, Object>> adminGetAllActivities(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<MerchantActivity> all = activityRepository.findAll();
            // 关联商家名称
            List<Map<String, Object>> result = all.stream().map(a -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", a.getId());
                item.put("title", a.getTitle());
                item.put("description", a.getDescription());
                item.put("type", a.getType());
                item.put("image", a.getImage());
                item.put("discount", a.getDiscount());
                item.put("status", a.getStatus());
                item.put("startTime", a.getStartTime());
                item.put("endTime", a.getEndTime());
                item.put("createdAt", a.getCreatedAt());
                if (a.getMerchant() != null) {
                    item.put("merchantId", a.getMerchant().getId());
                    item.put("merchantName", a.getMerchant().getShopName());
                }
                return item;
            }).filter(item -> {
                boolean matchKeyword = keyword.isEmpty() ||
                    item.getOrDefault("title", "").toString().contains(keyword) ||
                    item.getOrDefault("merchantName", "").toString().contains(keyword);
                boolean matchStatus = status.isEmpty() ||
                    item.getOrDefault("status", "").toString().equals(status);
                return matchKeyword && matchStatus;
            }).sorted((a, b) -> {
                // 按创建时间倒序
                Object ta = a.get("createdAt"), tb = b.get("createdAt");
                if (ta == null || tb == null) return 0;
                return tb.toString().compareTo(ta.toString());
            }).collect(java.util.stream.Collectors.toList());

            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 管理员：强制下架活动（将状态设为 ended）
     */
    @LogOperation("管理员强制下架店铺活动")
    @PutMapping("/admin/{id}/offline")
    public ResponseEntity<Map<String, Object>> adminOfflineActivity(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            MerchantActivity activity = activityRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("活动不存在"));
            activity.setStatus("ended");
            activityRepository.save(activity);
            response.put("success", true);
            response.put("message", "已强制下架");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}