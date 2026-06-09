package com.jdz.tourism.controller;

import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.service.ScenicSpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.math.BigDecimal;

import com.jdz.tourism.annotation.LogOperation;

@RestController
@RequestMapping("/api/scenic")
@CrossOrigin(origins = "*")
@Tag(name = "景点管理", description = "景点相关的API接口，包括查询、搜索、推荐等功能")
public class ScenicSpotController {
    
    @Autowired
    private ScenicSpotService scenicSpotService;
    
    /**
     * 获取所有景点
     */
    @Operation(summary = "获取所有景点", description = "获取系统中所有的景点信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取景点列表"),
        @ApiResponse(responseCode = "400", description = "请求失败")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllScenicSpots(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, name = "pageNum") Integer pageNum,
            @RequestParam(required = false, name = "pageSize") Integer pageSize,
            @RequestParam(required = false, name = "currentPage") Integer currentPage,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer resolvedPage = page != null ? page : (pageNum != null ? pageNum : currentPage);
            Integer resolvedSize = size != null ? size : pageSize;

            // 兼容 offset/limit 风格
            if (resolvedPage == null && offset != null && resolvedSize != null && resolvedSize > 0) {
                resolvedPage = (offset / resolvedSize) + 1;
            }

            // 如果传了分页参数，则走 SQL 分页（避免“每页都是前 N 条”）
            if (resolvedPage != null && resolvedSize != null) {
                Page<ScenicSpot> pageResult = scenicSpotService.getScenicSpotsPage(resolvedPage, resolvedSize, keyword, sortField, sortOrder);
                response.put("success", true);
                response.put("data", pageResult.getContent());
                response.put("total", pageResult.getTotalElements());
                response.put("page", resolvedPage);
                response.put("size", resolvedSize);
                response.put("pages", pageResult.getTotalPages());
                return ResponseEntity.ok(response);
            }

            // 兼容旧行为：不传分页参数则返回全量
            List<ScenicSpot> scenicSpots = scenicSpotService.getAllScenicSpots();
            response.put("success", true);
            response.put("data", scenicSpots);
            response.put("total", scenicSpots.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据ID获取景点详情
     */
    @Operation(summary = "根据ID获取景点详情", description = "通过景点ID获取具体的景点信息，会增加访问量")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功获取景点详情"),
        @ApiResponse(responseCode = "404", description = "景点不存在"),
        @ApiResponse(responseCode = "400", description = "请求失败")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getScenicSpotById(
            @Parameter(description = "景点ID", required = true) @PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 将字符串ID转换为Long
            Long scenicId;
            try {
                scenicId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                System.err.println("无效的景点ID格式: " + id);
                response.put("success", false);
                response.put("message", "无效的景点ID格式");
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("========== 获取景点详情: ID = " + scenicId + " (原始: " + id + ") ==========");
            Optional<ScenicSpot> scenicSpot = scenicSpotService.getScenicSpotById(scenicId);
            if (scenicSpot.isPresent()) {
                System.out.println("成功找到景点: " + scenicSpot.get().getName());
                response.put("success", true);
                response.put("data", scenicSpot.get());
                return ResponseEntity.ok(response);
            } else {
                System.out.println("未找到景点，ID = " + scenicId);
                response.put("success", false);
                response.put("message", "景点不存在");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            System.err.println("获取景点详情时发生异常，ID = " + id + ", 错误: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取推荐景点
     */
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendScenicSpots(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "8") int size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScenicSpot> scenicSpots = scenicSpotService.recommendScenicSpots(userId);

            // 有排序参数时，先全局排序再分页
            if (sortField != null && !sortField.isBlank()) {
                java.util.Comparator<ScenicSpot> cmp = switch (sortField) {
                    case "price" -> java.util.Comparator.comparingDouble(
                            s -> s.getPrice() != null ? s.getPrice().doubleValue() : 0.0);
                    case "rating" -> java.util.Comparator.comparingDouble(
                            s -> s.getRating() != null ? s.getRating().doubleValue() : 0.0);
                    case "favoriteCount" -> java.util.Comparator.comparingLong(
                            s -> s.getVisitCount() != null ? s.getVisitCount() : 0L);
                    default -> java.util.Comparator.comparingLong(s -> s.getId() != null ? s.getId() : 0L);
                };
                if (!"asc".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
                scenicSpots = scenicSpots.stream().sorted(cmp).collect(java.util.stream.Collectors.toList());
            }

            int total = scenicSpots.size();
            int fromIndex = Math.min((page - 1) * size, total);
            int toIndex = Math.min(fromIndex + size, total);
            List<ScenicSpot> pageData = scenicSpots.subList(fromIndex, toIndex);
            response.put("success", true);
            response.put("data", pageData);
            response.put("total", total);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取热门景点
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularScenicSpots() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScenicSpot> scenicSpots = scenicSpotService.getPopularScenicSpots();
            response.put("success", true);
            response.put("data", scenicSpots);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据分类获取景点
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getScenicSpotsByCategory(@PathVariable String category) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScenicSpot> scenicSpots = scenicSpotService.getScenicSpotsByCategory(category);
            response.put("success", true);
            response.put("data", scenicSpots);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据标签搜索景点
     */
    @GetMapping("/search/tag")
    public ResponseEntity<Map<String, Object>> searchScenicSpotsByTag(@RequestParam String tag) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScenicSpot> scenicSpots = scenicSpotService.getScenicSpotsByTag(tag);
            response.put("success", true);
            response.put("data", scenicSpots);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据名称搜索景点（优化版）
     */
    @GetMapping("/search/name")
    public ResponseEntity<Map<String, Object>> searchScenicSpotsByName(@RequestParam String name) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScenicSpot> scenicSpots = scenicSpotService.searchScenicSpotsByName(name);
            response.put("success", true);
            response.put("data", scenicSpots);
            response.put("message", "搜索完成，找到 " + scenicSpots.size() + " 个结果");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 综合搜索景点（关键词搜索：名称、描述、标签）
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchScenicSpots(@RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScenicSpot> scenicSpots = scenicSpotService.searchScenicSpotsByName(keyword);
            response.put("success", true);
            response.put("data", scenicSpots);
            response.put("message", "综合搜索完成，找到 " + scenicSpots.size() + " 个结果");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据价格范围搜索景点
     */
    @GetMapping("/search/price")
    public ResponseEntity<Map<String, Object>> searchScenicSpotsByPriceRange(
            @RequestParam Double minPrice, 
            @RequestParam Double maxPrice) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScenicSpot> scenicSpots = scenicSpotService.searchScenicSpotsByPriceRange(minPrice, maxPrice);
            response.put("success", true);
            response.put("data", scenicSpots);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 创建景点
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createScenicSpot(@RequestBody Map<String, Object> scenicData) {
        // 1. 尝试从请求体中获取ID，支持多种常见的ID字段名
        Long id = null;
        String[] idKeys = {"id", "scenicId", "scenicSpotId", "merchantId", "_id"};
        for (String key : idKeys) {
            if (scenicData.containsKey(key)) {
                Object val = scenicData.get(key);
                if (val != null) {
                    try {
                        long parsedId = Long.parseLong(val.toString());
                        if (parsedId > 0) {
                            id = parsedId;
                            System.out.println("检测到POST请求包含ID (" + key + ")=" + id + "，自动转换为更新操作");
                            break;
                        }
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }

        // 2. 如果没有ID，尝试通过名称查找是否存在同名景点
        // 这是一个兜底策略，防止用户在编辑模式下（但ID丢失）提交导致创建重复数据
        if (id == null && scenicData.containsKey("name")) {
            String name = scenicData.get("name").toString();
            Optional<ScenicSpot> existing = scenicSpotService.getScenicSpotByName(name);
            if (existing.isPresent()) {
                id = existing.get().getId();
                System.out.println("检测到同名景点 '" + name + "' (ID=" + id + ")，自动转换为更新操作");
            }
        }

        // 3. 如果找到了ID，执行更新操作
        if (id != null) {
            return updateScenicSpot(id, scenicData);
        }

        // 4. 执行正常的创建操作
        Map<String, Object> response = new HashMap<>();
        try {
            ScenicSpot scenicSpot = convertToScenicSpot(scenicData);
            ScenicSpot savedScenicSpot = scenicSpotService.createScenicSpot(scenicSpot);
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("data", savedScenicSpot);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新景点
     * 确保编辑时不会新增景点，景点就是商家（SCENIC分类）
     */
    @LogOperation("更新景点")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateScenicSpot(@PathVariable Long id, @RequestBody Map<String, Object> scenicData) {
        System.out.println("========== 收到更新景点请求: ID = " + id + " ==========");
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证ID有效性
            if (id == null || id <= 0) {
                System.err.println("景点ID无效: " + id);
                response.put("success", false);
                response.put("message", "景点ID无效");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查景点是否存在
            Optional<ScenicSpot> existingOpt = scenicSpotService.getScenicSpotById(id);
            if (!existingOpt.isPresent()) {
                System.err.println("景点不存在: ID = " + id);
                response.put("success", false);
                response.put("message", "景点不存在，ID: " + id + "。无法更新不存在的景点，请确保景点ID正确。");
                return ResponseEntity.status(404).body(response);
            }
            
            System.out.println("找到景点，准备更新: " + existingOpt.get().getName());
            
            // 转换并更新景点
            ScenicSpot scenicSpot = convertToScenicSpot(scenicData);
            scenicSpot.setId(id); // 确保使用路径参数中的ID
            ScenicSpot updatedScenicSpot = scenicSpotService.updateScenicSpot(scenicSpot);
            
            response.put("success", true);
            response.put("message", "景点更新成功");
            response.put("data", updatedScenicSpot);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 业务异常
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            // 其他异常
            System.err.println("更新景点时发生异常，ID: " + id + ", 错误: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 将Map数据转换为ScenicSpot实体
     * 注意：此方法用于更新操作，会设置所有提供的字段（包括空值）
     */
    private ScenicSpot convertToScenicSpot(Map<String, Object> data) {
        ScenicSpot scenicSpot = new ScenicSpot();
        
        // 使用containsKey检查字段是否存在，如果存在则设置（包括空字符串和null）
        if (data.containsKey("name")) {
            Object nameObj = data.get("name");
            scenicSpot.setName(nameObj != null ? nameObj.toString() : null);
        }
        if (data.containsKey("description")) {
            Object descObj = data.get("description");
            scenicSpot.setDescription(descObj != null ? descObj.toString() : null);
        }
        if (data.containsKey("price")) {
            Object priceObj = data.get("price");
            if (priceObj != null) {
                if (priceObj instanceof Number) {
                    scenicSpot.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                } else {
                    scenicSpot.setPrice(new BigDecimal(priceObj.toString()));
                }
            } else {
                scenicSpot.setPrice(BigDecimal.ZERO);
            }
        } else if (data.containsKey("ticketPrice")) {
            Object priceObj = data.get("ticketPrice");
            if (priceObj != null) {
                if (priceObj instanceof Number) {
                    scenicSpot.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                } else {
                    scenicSpot.setPrice(new BigDecimal(priceObj.toString()));
                }
            }
        }
        if (data.containsKey("openTime")) {
            Object openTimeObj = data.get("openTime");
            scenicSpot.setOpenTime(openTimeObj != null ? openTimeObj.toString() : null);
        }
        if (data.containsKey("openingHoursDetail")) {
            Object v = data.get("openingHoursDetail");
            scenicSpot.setOpeningHoursDetail(v != null ? v.toString() : null);
        } else if (data.containsKey("opening_hours_detail")) {
            Object v = data.get("opening_hours_detail");
            scenicSpot.setOpeningHoursDetail(v != null ? v.toString() : null);
        }
        if (data.containsKey("address")) {
            Object addressObj = data.get("address");
            scenicSpot.setAddress(addressObj != null ? addressObj.toString() : null);
        }
        if (data.containsKey("phone")) {
            Object phoneObj = data.get("phone");
            scenicSpot.setPhone(phoneObj != null ? phoneObj.toString() : null);
        } else if (data.containsKey("tel")) {
            Object phoneObj = data.get("tel");
            scenicSpot.setPhone(phoneObj != null ? phoneObj.toString() : null);
        }
        if (data.containsKey("officialPhone")) {
            Object v = data.get("officialPhone");
            scenicSpot.setOfficialPhone(v != null ? v.toString() : null);
        } else if (data.containsKey("official_phone")) {
            Object v = data.get("official_phone");
            scenicSpot.setOfficialPhone(v != null ? v.toString() : null);
        }
        if (data.containsKey("category")) {
            Object categoryObj = data.get("category");
            scenicSpot.setCategory(categoryObj != null ? categoryObj.toString() : null);
        }
        if (data.containsKey("tags")) {
            Object tagsObj = data.get("tags");
            scenicSpot.setTags(tagsObj != null ? tagsObj.toString() : null);
        }
        // 公告与公告详情（兼容多字段别名）
        if (data.containsKey("notice")) {
            Object v = data.get("notice");
            scenicSpot.setNotice(v != null ? v.toString() : null);
        }
        if (data.containsKey("noticeDetail")) {
            Object v = data.get("noticeDetail");
            scenicSpot.setNoticeDetail(v != null ? v.toString() : null);
        } else if (data.containsKey("noticeContent")) {
            Object v = data.get("noticeContent");
            scenicSpot.setNoticeDetail(v != null ? v.toString() : null);
        } else if (data.containsKey("publicNotice")) {
            Object v = data.get("publicNotice");
            scenicSpot.setNoticeDetail(v != null ? v.toString() : null);
        } else if (data.containsKey("ticketInfo")) {
            Object v = data.get("ticketInfo");
            scenicSpot.setNoticeDetail(v != null ? v.toString() : null);
        }
        // 门票信息（独立字段）
        if (data.containsKey("ticketInfo")) {
            Object v = data.get("ticketInfo");
            scenicSpot.setTicketInfo(v != null ? v.toString() : null);
        }
        // 优待政策
        if (data.containsKey("preferentialPolicy")) {
            Object v = data.get("preferentialPolicy");
            scenicSpot.setPreferentialPolicy(v != null ? v.toString() : null);
        } else if (data.containsKey("preferential_policy")) {
            Object v = data.get("preferential_policy");
            scenicSpot.setPreferentialPolicy(v != null ? v.toString() : null);
        }
        // 服务设施详情
        if (data.containsKey("facilitiesDetail")) {
            Object v = data.get("facilitiesDetail");
            scenicSpot.setFacilitiesDetail(v != null ? v.toString() : null);
        } else if (data.containsKey("facilities_detail")) {
            Object v = data.get("facilities_detail");
            scenicSpot.setFacilitiesDetail(v != null ? v.toString() : null);
        }
        // 景点特色
        if (data.containsKey("features")) {
            Object v = data.get("features");
            scenicSpot.setFeatures(v != null ? v.toString() : null);
        }
        // 亮点推荐
        if (data.containsKey("highlights")) {
            Object v = data.get("highlights");
            scenicSpot.setHighlights(v != null ? v.toString() : null);
        }
        // 游玩提示
        if (data.containsKey("tips")) {
            Object v = data.get("tips");
            scenicSpot.setTips(v != null ? v.toString() : null);
        }
        if (data.containsKey("latitude")) {
            Object latObj = data.get("latitude");
            if (latObj != null) {
                scenicSpot.setLatitude(new BigDecimal(latObj.toString()));
            } else {
                scenicSpot.setLatitude(null);
            }
        }
        if (data.containsKey("longitude")) {
            Object lngObj = data.get("longitude");
            if (lngObj != null) {
                scenicSpot.setLongitude(new BigDecimal(lngObj.toString()));
            } else {
                scenicSpot.setLongitude(null);
            }
        }
        
        // 处理评分：管理员可以手动设置评分
        if (data.containsKey("rating")) {
            Object ratingObj = data.get("rating");
            if (ratingObj != null) {
                try {
                    if (ratingObj instanceof Number) {
                        double ratingValue = ((Number) ratingObj).doubleValue();
                        // 限制评分范围在0-5之间
                        if (ratingValue < 0) ratingValue = 0;
                        if (ratingValue > 5) ratingValue = 5;
                        scenicSpot.setRating(BigDecimal.valueOf(ratingValue).setScale(2, java.math.RoundingMode.HALF_UP));
                    } else {
                        double ratingValue = Double.parseDouble(ratingObj.toString());
                        if (ratingValue < 0) ratingValue = 0;
                        if (ratingValue > 5) ratingValue = 5;
                        scenicSpot.setRating(BigDecimal.valueOf(ratingValue).setScale(2, java.math.RoundingMode.HALF_UP));
                    }
                } catch (Exception e) {
                    // 如果解析失败，不设置评分（将使用自动计算的评分）
                    System.err.println("解析评分失败: " + e.getMessage());
                }
            }
        }
        
        // 处理图片：支持单张图片或多张图片
        if (data.containsKey("images")) {
            Object imagesObj = data.get("images");
            if (imagesObj instanceof List<?>) {
                // 如果是数组，转换为逗号分隔的字符串
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) imagesObj;
                String joined = list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .filter(s -> !s.isBlank())
                        .distinct()
                        .collect(java.util.stream.Collectors.joining(","));
                scenicSpot.setScenicImages(joined.isEmpty() ? null : joined);
                // 第一张图片作为封面
                if (!joined.isEmpty()) {
                    String firstImage = joined.split(",")[0].trim();
                    scenicSpot.setImage(firstImage);
                }
            } else if (imagesObj != null) {
                // 如果是字符串，直接使用
                String imagesStr = imagesObj.toString();
                scenicSpot.setScenicImages(imagesStr);
                if (!imagesStr.isEmpty()) {
                    String firstImage = imagesStr.split(",")[0].trim();
                    scenicSpot.setImage(firstImage);
                }
            }
        } else if (data.containsKey("image")) {
            // 兼容单张图片的情况
            String image = data.get("image") != null ? data.get("image").toString() : null;
            scenicSpot.setImage(image);
        }
        
        return scenicSpot;
    }
    
    /**
     * 删除景点
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteScenicSpot(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            scenicSpotService.deleteScenicSpot(id);
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
