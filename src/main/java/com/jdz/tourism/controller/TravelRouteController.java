package com.jdz.tourism.controller;

import com.jdz.tourism.entity.TravelRoute;
import com.jdz.tourism.service.TravelRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.jdz.tourism.annotation.LogOperation;

@RestController
@RequestMapping("/api/route")
@CrossOrigin(origins = "*")
@Tag(name = "路线管理", description = "旅游路线推荐相关的API接口")
public class TravelRouteController {
    
    @Autowired
    private TravelRouteService travelRouteService;
    
    /**
     * 获取所有路线（管理员用，不限制数量、不降级）
     */
    @Operation(summary = "获取全部路线列表", description = "管理员获取所有路线，不做推荐过滤")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoutes() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TravelRoute> routes = travelRouteService.getAllRoutes();
            response.put("success", true);
            response.put("data", routes);
            response.put("total", routes.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取所有路线推荐（增强版：使用智能推荐算法）
     */
    @Operation(summary = "获取路线推荐列表", description = "获取所有旅游路线推荐信息，支持智能推荐算法")
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendRoutes(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TravelRoute> routes = userId != null
                    ? travelRouteService.recommendRoutesByUser(userId, days, difficulty)
                    : travelRouteService.getPopularRoutes(days, difficulty);
            response.put("success", true);
            response.put("data", routes);
            response.put("total", routes.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取路线详情
     */
    @Operation(summary = "获取路线详情", description = "根据ID获取路线详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRouteDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            return travelRouteService.getRouteById(id)
                    .map(route -> {
                        response.put("success", true);
                        response.put("data", route);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("success", false);
                        response.put("message", "路线不存在");
                        return ResponseEntity.badRequest().body(response);
                    });
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 创建路线
     */
    @Operation(summary = "创建路线", description = "创建新的旅游路线")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRoute(@RequestBody Map<String, Object> routeData) {
        Map<String, Object> response = new HashMap<>();
        try {
            TravelRoute route = convertToTravelRoute(routeData);
            TravelRoute savedRoute = travelRouteService.createRoute(route);
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("data", savedRoute);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新路线
     */
    @Operation(summary = "更新路线", description = "根据ID更新路线信息")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRoute(@PathVariable Long id, @RequestBody Map<String, Object> routeData) {
        Map<String, Object> response = new HashMap<>();
        try {
            TravelRoute route = convertToTravelRoute(routeData);
            route.setId(id);
            TravelRoute updatedRoute = travelRouteService.updateRoute(route);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", updatedRoute);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 将Map数据转换为TravelRoute实体
     */
    private TravelRoute convertToTravelRoute(Map<String, Object> data) {
        TravelRoute route = new TravelRoute();
        
        if (data.containsKey("name")) {
            route.setName(data.get("name") != null ? data.get("name").toString() : null);
        }
        if (data.containsKey("description")) {
            route.setDescription(data.get("description") != null ? data.get("description").toString() : null);
        }
        if (data.containsKey("days")) {
            Object daysObj = data.get("days");
            if (daysObj != null) {
                if (daysObj instanceof Number) {
                    route.setDays(((Number) daysObj).intValue());
                } else {
                    route.setDays(Integer.parseInt(daysObj.toString()));
                }
            }
        }
        if (data.containsKey("difficulty")) {
            route.setDifficulty(data.get("difficulty") != null ? data.get("difficulty").toString() : null);
        }
        if (data.containsKey("totalPrice")) {
            Object priceObj = data.get("totalPrice");
            if (priceObj != null) {
                if (priceObj instanceof Number) {
                    route.setTotalPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                } else {
                    route.setTotalPrice(new BigDecimal(priceObj.toString()));
                }
            } else {
                route.setTotalPrice(BigDecimal.ZERO);
            }
        }
        if (data.containsKey("tags")) {
            route.setTags(data.get("tags") != null ? data.get("tags").toString() : null);
        }
        if (data.containsKey("image")) {
            route.setImage(data.get("image") != null ? data.get("image").toString() : null);
        }
        if (data.containsKey("scenicSpots")) {
            Object scenicSpotsObj = data.get("scenicSpots");
            if (scenicSpotsObj != null) {
                // 如果是字符串，直接使用；如果是对象，转换为JSON字符串
                if (scenicSpotsObj instanceof String) {
                    route.setScenicSpots(scenicSpotsObj.toString());
                } else {
                    // 如果是对象或数组，转换为JSON字符串
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        route.setScenicSpots(objectMapper.writeValueAsString(scenicSpotsObj));
                    } catch (Exception e) {
                        route.setScenicSpots(scenicSpotsObj.toString());
                    }
                }
            } else {
                route.setScenicSpots(null);
            }
        }
        
        return route;
    }
    
    /**
     * 删除路线
     */
    @Operation(summary = "删除路线", description = "根据ID删除路线信息")
    @LogOperation("删除路线")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoute(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!travelRouteService.getRouteById(id).isPresent()) {
                response.put("success", false);
                response.put("message", "路线不存在");
                return ResponseEntity.badRequest().body(response);
            }
            travelRouteService.deleteRoute(id);
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

