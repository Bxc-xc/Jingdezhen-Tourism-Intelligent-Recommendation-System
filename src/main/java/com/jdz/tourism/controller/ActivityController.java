package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Activity;
import com.jdz.tourism.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {
    
    @Autowired
    private ActivityService activityService;
    
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<Map<String, Object>> getActivitiesByMerchant(@PathVariable Long merchantId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Activity> activities = activityService.getActiveActivitiesByMerchantId(merchantId);
            response.put("success", true);
            response.put("data", activities);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getAllActiveActivities() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Activity> activities = activityService.getAllActiveActivities();
            response.put("success", true);
            response.put("data", activities);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getActivityById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            return activityService.getActivityById(id)
                .map(activity -> {
                    response.put("success", true);
                    response.put("data", activity);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/merchant/{merchantId}")
    public ResponseEntity<Map<String, Object>> createActivity(@PathVariable Long merchantId, @RequestBody Activity activity) {
        Map<String, Object> response = new HashMap<>();
        try {
            Activity created = activityService.createActivity(merchantId, activity);
            response.put("success", true);
            response.put("data", created);
            response.put("message", "活动创建成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        Map<String, Object> response = new HashMap<>();
        try {
            Activity updated = activityService.updateActivity(id, activity);
            response.put("success", true);
            response.put("data", updated);
            response.put("message", "活动更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteActivity(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            activityService.deleteActivity(id);
            response.put("success", true);
            response.put("message", "活动删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

