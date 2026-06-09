package com.jdz.tourism.controller;

import com.jdz.tourism.entity.TravelPlan;
import com.jdz.tourism.service.TravelPlanService;
import com.jdz.tourism.service.MapRouteService;
import com.jdz.tourism.service.SmartTripService;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;

import com.jdz.tourism.entity.TravelPlanVersion;

@RestController
@RequestMapping("/api/plan")
@CrossOrigin(origins = "*")
public class TravelPlanController {
    
    @Autowired
    private TravelPlanService travelPlanService;
    
    @Autowired
    private MapRouteService mapRouteService;

    @Autowired
    private SmartTripService smartTripService;
    
    /**
     * 手动创建行程（不走 AI 生成，直接保存用户填写的内容）
     */
    @LogOperation("手动创建行程")
    @PostMapping("/manual")
    public ResponseEntity<Map<String, Object>> createManualPlan(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Object userIdObj = request.get("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "userId 参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            Long userId = Long.valueOf(userIdObj.toString());

            // 天数
            Object daysObj = request.get("days");
            int days = daysObj != null ? Integer.parseInt(daysObj.toString()) : 1;

            // 出发日期（可选，默认今天）
            String startDateStr = request.get("startDate") != null ? request.get("startDate").toString() : null;
            LocalDate startDate = (startDateStr != null && !startDateStr.isEmpty())
                    ? LocalDate.parse(startDateStr)
                    : LocalDate.now();
            LocalDate endDate = startDate.plusDays(days - 1);

            // 标题
            String title = request.get("title") != null ? request.get("title").toString() : "手动创建行程";

            // planDetails（前端传来的 JSON 字符串）
            String planDetails = request.get("manualPlanDetails") != null
                    ? request.get("manualPlanDetails").toString()
                    : (request.get("planDetails") != null ? request.get("planDetails").toString() : "{}");

            Map<String, Object> planRequest = new HashMap<>();
            planRequest.put("userId", userId);
            planRequest.put("title", title);
            planRequest.put("days", days);
            planRequest.put("startDate", startDate.toString());
            planRequest.put("planDetails", planDetails);
            planRequest.put("budgetType", "medium");
            planRequest.put("transportType", "walking");

            TravelPlan plan = travelPlanService.generatePlan(userId, planRequest);
            response.put("success", true);
            response.put("message", "行程创建成功");
            response.put("data", plan);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 生成行程规划
     */
    @LogOperation("生成行程")
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePlan(@RequestBody Map<String, Object> planRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 安全获取 userId
            Object userIdObj = planRequest.get("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "userId 参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            Long userId = Long.valueOf(userIdObj.toString());
            
            TravelPlan plan = travelPlanService.generatePlan(userId, planRequest);
            response.put("success", true);
            response.put("message", "行程生成成功");
            response.put("data", plan);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取用户行程列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPlans(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TravelPlan> plans = travelPlanService.getUserPlans(userId);
            response.put("success", true);
            response.put("data", plans);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取行程详情
     */
    @GetMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> getPlanById(@PathVariable Long planId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<TravelPlan> plan = travelPlanService.getPlanById(planId);
            if (plan.isPresent()) {
                response.put("success", true);
                response.put("data", plan.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "行程不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新行程
     */
    @LogOperation("更新行程")
    @PutMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> updatePlan(@PathVariable Long planId, @RequestBody TravelPlan planData) {
        Map<String, Object> response = new HashMap<>();
        try {
            TravelPlan updatedPlan = travelPlanService.updatePlan(planId, planData);
            response.put("success", true);
            response.put("message", "行程更新成功");
            response.put("data", updatedPlan);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除行程
     */
    @LogOperation("删除行程")
    @DeleteMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> deletePlan(@PathVariable Long planId) {
        Map<String, Object> response = new HashMap<>();
        try {
            travelPlanService.deletePlan(planId);
            response.put("success", true);
            response.put("message", "行程删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取行程的版本列表
     */
    @GetMapping("/{planId}/versions")
    public ResponseEntity<Map<String, Object>> getPlanVersions(@PathVariable Long planId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TravelPlanVersion> versions = travelPlanService.getPlanVersions(planId);
            response.put("success", true);
            response.put("data", versions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 从指定版本恢复行程
     */
    @LogOperation("恢复行程版本")
    @PostMapping("/{planId}/restore/{versionId}")
    public ResponseEntity<Map<String, Object>> restorePlanFromVersion(@PathVariable Long planId, @PathVariable Long versionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            TravelPlan restored = travelPlanService.restoreFromVersion(planId, versionId);
            response.put("success", true);
            response.put("message", "行程已从指定版本恢复");
            response.put("data", restored);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 基于推荐路线创建行程（模板功能）
     */
    @LogOperation("基于推荐路线创建行程")
    @PostMapping("/from-route")
    public ResponseEntity<Map<String, Object>> createPlanFromRoute(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 安全获取 userId
            Object userIdObj = request.get("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "userId 参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            Long userId = Long.valueOf(userIdObj.toString());
            
            // 安全获取 routeId
            Object routeIdObj = request.get("routeId");
            if (routeIdObj == null) {
                response.put("success", false);
                response.put("message", "routeId 参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            Long routeId = Long.valueOf(routeIdObj.toString());
            
            // 安全获取 startDate
            LocalDate startDate = null;
            Object startDateObj = request.get("startDate");
            if (startDateObj != null) {
                startDate = LocalDate.parse(startDateObj.toString());
            }
            
            TravelPlan plan = travelPlanService.createPlanFromRoute(userId, routeId, startDate);
            response.put("success", true);
            response.put("message", "行程创建成功");
            response.put("data", plan);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 智能生成行程（前端直接调用，不保存到数据库，仅返回行程数据供预览）
     */
    @PostMapping("/smart-generate")
    public ResponseEntity<Map<String, Object>> smartGenerate(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int days = request.get("days") != null ? Integer.parseInt(request.get("days").toString()) : 2;
            @SuppressWarnings("unchecked")
            java.util.List<String> preferences = request.get("preferences") instanceof java.util.List
                    ? (java.util.List<String>) request.get("preferences")
                    : new java.util.ArrayList<>();
            String budget    = request.get("budget") != null ? request.get("budget").toString() : null;
            String startDate = request.get("startDate") != null ? request.get("startDate").toString() : null;

            java.util.List<SmartTripService.DayPlan> plans =
                    smartTripService.generateTrip(days, preferences, budget, startDate);

            response.put("success", true);
            response.put("data", plans);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取起终点之间的推荐交通方案
     * 支持多种方式：
     * 1. 通过景点名称（originName, destName）
     * 2. 通过景点ID（originId, destId）
     * 3. 通过经纬度（originLat, originLng, destLat, destLng）
     */
    @GetMapping("/transport-options")
    public ResponseEntity<Map<String, Object>> getTransportOptions(
            @RequestParam(required = false) String originName,
            @RequestParam(required = false) String destName,
            @RequestParam(required = false) Long originId,
            @RequestParam(required = false) Long destId,
            @RequestParam(required = false) Double originLat,
            @RequestParam(required = false) Double originLng,
            @RequestParam(required = false) Double destLat,
            @RequestParam(required = false) Double destLng) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> options;
            
            // 优先使用经纬度（最精确）
            if (originLat != null && originLng != null && destLat != null && destLng != null) {
                String origin = (originName != null && !originName.isEmpty()) ? originName : "起点";
                String dest = (destName != null && !destName.isEmpty()) ? destName : "终点";
                options = mapRouteService.getTransportOptionsByCoordinates(
                    originLat, originLng, destLat, destLng, origin, dest);
            }
            // 其次使用景点ID
            else if (originId != null && destId != null && originId > 0 && destId > 0) {
                options = mapRouteService.getTransportOptionsByScenicIds(originId, destId);
            }
            // 最后使用景点名称
            else if (originName != null && !originName.isEmpty() && 
                     destName != null && !destName.isEmpty()) {
                options = mapRouteService.getTransportOptions(originName, destName);
            }
            else {
                response.put("success", false);
                response.put("message", "请提供起终点信息（名称、ID或经纬度）。" +
                    "当前参数：originName=" + originName + ", destName=" + destName +
                    ", originId=" + originId + ", destId=" + destId +
                    ", originLat=" + originLat + ", originLng=" + originLng +
                    ", destLat=" + destLat + ", destLng=" + destLng);
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("data", options);
            response.put("total", options.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取交通方案失败: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            return ResponseEntity.badRequest().body(response);
        }
    }
}