package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Review;
import com.jdz.tourism.service.ReviewService;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.jdz.tourism.utils.JwtUtil;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired(required = false)
    private HttpServletRequest request;
    
    @Autowired(required = false)
    private JwtUtil jwtUtil;
    
    private Long resolveCurrentUserId() {
        try {
            // 最可靠：从 Authorization: Bearer <token> 中解析 userId
            String authorization = request != null ? request.getHeader("Authorization") : null;
            if (authorization != null && authorization.startsWith("Bearer ") && jwtUtil != null) {
                String token = authorization.substring(7);
                if (jwtUtil.validateToken(token)) {
                    return jwtUtil.extractUserId(token);
                }
            }
            // 兼容：如果前端/网关额外透传了 userId
            String v = request != null ? request.getHeader("X-User-Id") : null;
            if (v != null && !v.isBlank()) return Long.valueOf(v.trim());
        } catch (Exception ignore) {}
        return null;
    }
    private boolean isAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext() != null ? SecurityContextHolder.getContext().getAuthentication() : null;
            if (auth != null && auth.isAuthenticated()) {
                for (GrantedAuthority ga : auth.getAuthorities()) {
                    String r = ga.getAuthority();
                    if ("ROLE_ADMIN".equalsIgnoreCase(r) || "ADMIN".equalsIgnoreCase(r)) return true;
                }
            }
            String v = request != null ? request.getHeader("X-User-Role") : null;
            if (v != null && !v.isBlank()) return "ADMIN".equalsIgnoreCase(v.trim());
        } catch (Exception ignore) {}
        return false;
    }
    
    /**
     * 创建评论（支持景点或商家）
     */
    @LogOperation("发布评论")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody Map<String, Object> reviewData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(reviewData.get("userId").toString());
            String content = reviewData.get("content").toString();
            Integer rating = Integer.valueOf(reviewData.get("rating").toString());
            // 可选图片：数组或字符串，统一转成逗号分隔存储
            String images = null;
            if (reviewData.containsKey("images") && reviewData.get("images") != null) {
                Object imagesObj = reviewData.get("images");
                if (imagesObj instanceof java.util.List<?>) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Object> list = (java.util.List<Object>) imagesObj;
                    images = list.stream()
                            .filter(java.util.Objects::nonNull)
                            .map(Object::toString)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .collect(java.util.stream.Collectors.joining(","));
                } else {
                    images = imagesObj.toString();
                }
            }
            
            Review review;
            // 判断是景点评论还是商家评论（支持商家ID访问景点的兼容场景）
            if (reviewData.containsKey("scenicSpotId") && reviewData.get("scenicSpotId") != null) {
                Long scenicSpotId = Long.valueOf(reviewData.get("scenicSpotId").toString());
                review = reviewService.createReview(userId, scenicSpotId, content, rating, images);
            } else if (reviewData.containsKey("merchantId") && reviewData.get("merchantId") != null) {
                Long merchantId = Long.valueOf(reviewData.get("merchantId").toString());
                review = reviewService.createMerchantReview(userId, merchantId, content, rating, images);
            } else {
                response.put("success", false);
                response.put("message", "必须指定景点ID或商家ID");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 根据实际创建的评论对象推送最新评分
            if (review.getScenicSpot() != null) {
                reviewService.notifyRatingUpdateForScenicSpot(review.getScenicSpot().getId());
            }
            if (review.getMerchant() != null) {
                reviewService.notifyRatingUpdateForMerchant(review.getMerchant().getId());
            }
            
            response.put("success", true);
            response.put("message", "评论发布成功");
            response.put("data", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据景点ID获取评论列表
     */
    @GetMapping("/scenic/{scenicSpotId}")
    public ResponseEntity<Map<String, Object>> getReviewsByScenicSpotId(@PathVariable Long scenicSpotId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Review> reviews = reviewService.getReviewsByScenicSpotId(scenicSpotId);
            response.put("success", true);
            response.put("data", reviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据用户ID获取评论列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getReviewsByUserId(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Review> reviews = reviewService.getReviewsByUserId(userId);
            response.put("success", true);
            response.put("data", reviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据景点ID和评分获取评论列表
     */
    @GetMapping("/scenic/{scenicSpotId}/rating/{rating}")
    public ResponseEntity<Map<String, Object>> getReviewsByScenicSpotIdAndRating(
            @PathVariable Long scenicSpotId, 
            @PathVariable Integer rating) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Review> reviews = reviewService.getReviewsByScenicSpotIdAndRating(scenicSpotId, rating);
            response.put("success", true);
            response.put("data", reviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取景点的平均评分
     */
    @GetMapping("/scenic/{scenicSpotId}/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRatingByScenicSpotId(@PathVariable Long scenicSpotId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Double avgRating = reviewService.getAverageRatingByScenicSpotId(scenicSpotId);
            response.put("success", true);
            response.put("data", avgRating);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取景点的评论数量
     */
    @GetMapping("/scenic/{scenicSpotId}/count")
    public ResponseEntity<Map<String, Object>> getReviewCountByScenicSpotId(@PathVariable Long scenicSpotId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long count = reviewService.getReviewCountByScenicSpotId(scenicSpotId);
            response.put("success", true);
            response.put("data", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据ID获取评论详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReviewById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Review> review = reviewService.getReviewById(id);
            if (review.isPresent()) {
                response.put("success", true);
                response.put("data", review.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "评论不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新评论
     */
    @LogOperation("更新评论")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReview(@PathVariable Long id, @Valid @RequestBody Review review) {
        Map<String, Object> response = new HashMap<>();
        try {
            review.setId(id);
            Review updatedReview = reviewService.updateReview(review);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", updatedReview);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除评论
     */
    @LogOperation("删除评论")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Review> reviewOpt = reviewService.getReviewById(id);
            if (reviewOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "评论不存在");
                return ResponseEntity.badRequest().body(response);
            }
            Review review = reviewOpt.get();
            Long ownerId = review.getUser() != null ? review.getUser().getId() : null;
            Long currentUserId = resolveCurrentUserId();
            boolean admin = isAdmin();
            // 管理员可直接删除；普通用户只能删自己的
            if (!admin && (currentUserId == null || ownerId == null || !ownerId.equals(currentUserId))) {
                response.put("success", false);
                response.put("message", "无权限删除此评价");
                return ResponseEntity.status(403).body(response);
            }
            reviewService.deleteReview(id);
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
     * 批量删除评论
     */
    @LogOperation("批量删除评论")
    @PostMapping("/batch-delete")
    public ResponseEntity<Map<String, Object>> batchDeleteReviews(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 管理员校验：从 token 中解析角色
            String authorization = request != null ? request.getHeader("Authorization") : null;
            boolean admin = isAdmin();
            if (!admin && authorization != null && authorization.startsWith("Bearer ") && jwtUtil != null) {
                String token = authorization.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String role = jwtUtil.extractRole(token);
                    admin = "ADMIN".equalsIgnoreCase(role);
                }
            }
            if (!admin) {
                response.put("success", false);
                response.put("message", "无权限删除评论");
                return ResponseEntity.status(403).body(response);
            }

            Object idsObj = body.get("ids");
            if (!(idsObj instanceof java.util.List<?>)) {
                response.put("success", false);
                response.put("message", "删除ID列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            java.util.List<?> raw = (java.util.List<?>) idsObj;
            java.util.List<Long> ids = new java.util.ArrayList<>();
            for (Object o : raw) {
                if (o instanceof Number) ids.add(((Number) o).longValue());
                else if (o != null) ids.add(Long.parseLong(o.toString()));
            }
            if (ids.isEmpty()) {
                response.put("success", false);
                response.put("message", "删除ID列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            java.util.List<Long> failed = new java.util.ArrayList<>();
            try {
                reviewService.deleteReviews(ids);
            } catch (Exception e) {
                for (Long rid : ids) {
                    try { reviewService.deleteReview(rid); } catch (Exception ex) { failed.add(rid); }
                }
            }
            response.put("success", failed.isEmpty());
            response.put("message", failed.isEmpty() ? "批量删除成功" : "部分删除失败");
            response.put("failedIds", failed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 回复评论
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<Map<String, Object>> replyReview(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String content = body.get("content");
            Review updated = reviewService.replyReview(id, content);
            response.put("success", true);
            response.put("message", "回复成功");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取所有评论
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReviews() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Review> reviews = reviewService.getAllReviews();
            response.put("success", true);
            response.put("data", reviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 创建商家评论
     */
    @LogOperation("发布商家评论")
    @PostMapping("/merchant")
    public ResponseEntity<Map<String, Object>> createMerchantReview(@RequestBody Map<String, Object> reviewData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(reviewData.get("userId").toString());
            Long merchantId = Long.valueOf(reviewData.get("merchantId").toString());
            String content = reviewData.get("content").toString();
            Integer rating = Integer.valueOf(reviewData.get("rating").toString());
            // 可选图片同上
            String images = null;
            if (reviewData.containsKey("images") && reviewData.get("images") != null) {
                Object imagesObj = reviewData.get("images");
                if (imagesObj instanceof java.util.List<?>) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Object> list = (java.util.List<Object>) imagesObj;
                    images = list.stream()
                            .filter(java.util.Objects::nonNull)
                            .map(Object::toString)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .collect(java.util.stream.Collectors.joining(","));
                } else {
                    images = imagesObj.toString();
                }
            }
            
            Review review = reviewService.createMerchantReview(userId, merchantId, content, rating, images);
            if (review.getScenicSpot() != null && review.getScenicSpot().getId() != null) {
                reviewService.notifyRatingUpdateForScenicSpot(review.getScenicSpot().getId());
            }
            if (review.getMerchant() != null && review.getMerchant().getId() != null) {
                reviewService.notifyRatingUpdateForMerchant(review.getMerchant().getId());
            }
            response.put("success", true);
            response.put("message", "评论发布成功");
            response.put("data", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据商家ID获取评论列表
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<Map<String, Object>> getReviewsByMerchantId(@PathVariable Long merchantId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Review> reviews = reviewService.getReviewsForMerchantDashboard(merchantId);
            response.put("success", true);
            response.put("data", reviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据商家ID和评分获取评论列表
     */
    @GetMapping("/merchant/{merchantId}/rating/{rating}")
    public ResponseEntity<Map<String, Object>> getReviewsByMerchantIdAndRating(
            @PathVariable Long merchantId, 
            @PathVariable Integer rating) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Review> reviews = reviewService.getReviewsForMerchantDashboardByRating(merchantId, rating);
            response.put("success", true);
            response.put("data", reviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取商家的平均评分
     */
    @GetMapping("/merchant/{merchantId}/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRatingByMerchantId(@PathVariable Long merchantId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Double avgRating = reviewService.getAverageRatingByMerchantId(merchantId);
            response.put("success", true);
            response.put("data", avgRating);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取商家的评论数量
     */
    @GetMapping("/merchant/{merchantId}/count")
    public ResponseEntity<Map<String, Object>> getReviewCountByMerchantId(@PathVariable Long merchantId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long count = reviewService.getReviewCountByMerchantId(merchantId);
            response.put("success", true);
            response.put("data", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}