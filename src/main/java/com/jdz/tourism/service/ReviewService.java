package com.jdz.tourism.service;

import com.jdz.tourism.entity.Review;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.repository.ReviewRepository;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.repository.ScenicSpotRepository;
import com.jdz.tourism.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ScenicSpotRepository scenicSpotRepository;
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    @Autowired(required = false)
    private RealtimeDataService realtimeDataService;
    
    @Autowired(required = false)
    @Lazy
    private MerchantService merchantService;
    
    @Autowired(required = false)
    @Lazy
    private ScenicSpotService scenicSpotService;
    
    
    /**
     * 创建评论
     */
    public Review createReview(Long userId, Long scenicSpotId, String content, Integer rating) {
        return createReview(userId, scenicSpotId, content, rating, null);
    }

    /**
     * 创建评论（可带图片）
     */
    public Review createReview(Long userId, Long scenicSpotId, String content, Integer rating, String images) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证评分范围
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }
        
        // 先尝试按景点评论处理（支持通过ScenicSpotService查找，兼容商家体系生成的景点）
        ScenicSpot scenicSpot = null;
        if (scenicSpotService != null) {
            Optional<ScenicSpot> spotOpt = scenicSpotService.getScenicSpotById(scenicSpotId);
            if (spotOpt.isPresent()) {
                scenicSpot = spotOpt.get();
            }
        }
        if (scenicSpot == null) {
            scenicSpot = scenicSpotRepository.findById(scenicSpotId).orElse(null);
        }
        
        // 🔑 关键修复：检查scenicSpot的ID是否在scenic_spot表中真实存在
        // 如果scenicSpot是从商家转换来的虚拟对象（商家未关联scenic_spot），
        // 它的ID是商家ID，不在scenic_spot表中，会导致外键约束失败
        if (scenicSpot != null) {
            // 验证scenicSpot.getId()是否在scenic_spot表中真实存在
            boolean scenicIdExists = scenicSpotRepository.existsById(scenicSpot.getId());
            
            if (scenicIdExists) {
                // ✅ ID在scenic_spot表中存在，可以安全创建景点评论
                Review review = new Review();
                review.setUser(user);
                review.setScenicSpot(scenicSpot);
                review.setContent(content);
                review.setRating(rating);
                review.setCreateTime(java.time.LocalDateTime.now());
                if (images != null) {
                    review.setImages(images);
                }
                
                Review savedReview = reviewRepository.save(review);
                
                // 通知评分更新
                if (scenicSpot.getId() != null) {
                    notifyRatingUpdateForScenicSpot(scenicSpot.getId());
                }
                
                return savedReview;
            } else {
                // ⚠️ ID不在scenic_spot表中，说明这是从商家转换来的虚拟对象
                // 需要找到对应的商家，创建商家评论
                Optional<Merchant> merchantOpt = merchantRepository.findById(scenicSpot.getId());
                if (merchantOpt.isPresent()) {
                    Merchant merchant = merchantOpt.get();
                    if ("SCENIC".equals(merchant.getCategory())) {
                        return createMerchantReview(userId, merchant.getId(), content, rating, images);
                    }
                }
            }
        }
        
        // 如果找不到景点，尝试按商家评论处理（兼容景点商家化、使用商家ID访问的场景）
        Optional<Merchant> merchantOpt = merchantRepository.findById(scenicSpotId);
        if (merchantOpt.isPresent()) {
            Merchant merchant = merchantOpt.get();
            // 只允许景点类商家走此分支
            if ("SCENIC".equals(merchant.getCategory())) {
                return createMerchantReview(userId, merchant.getId(), content, rating, images);
            }
        }
        
        // 仍然找不到，则报错
        throw new RuntimeException("景点不存在");
    }
    
    /**
     * 根据景点ID获取评论列表
     */
    public List<Review> getReviewsByScenicSpotId(Long scenicSpotId) {
        return reviewRepository.findByScenicSpotId(scenicSpotId);
    }
    
    /**
     * 根据用户ID获取评论列表
     */
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
    
    /**
     * 根据景点ID和评分获取评论列表
     */
    public List<Review> getReviewsByScenicSpotIdAndRating(Long scenicSpotId, Integer rating) {
        return reviewRepository.findByScenicSpotIdAndRating(scenicSpotId, rating);
    }
    
    /**
     * 获取景点的平均评分
     */
    public Double getAverageRatingByScenicSpotId(Long scenicSpotId) {
        Double avgRating = reviewRepository.getAverageRatingByScenicSpotId(scenicSpotId);
        return avgRating != null ? avgRating : 0.0;
    }
    
    /**
     * 获取景点的评论数量
     */
    public Long getReviewCountByScenicSpotId(Long scenicSpotId) {
        return reviewRepository.countByScenicSpotId(scenicSpotId);
    }
    
    /**
     * 根据ID获取评论详情
     */
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }
    
    /**
     * 更新评论
     */
    public Review updateReview(Review review) {
        // 先查询现有的评价
        Review existingReview = reviewRepository.findById(review.getId())
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        // 保存旧的评分值，用于判断是否需要通知评分更新
        Integer oldRating = existingReview.getRating();
        boolean ratingChanged = false;
        
        // 只更新允许更新的字段
        if (review.getRating() != null) {
            // 验证评分范围
            if (review.getRating() < 1 || review.getRating() > 5) {
                throw new RuntimeException("评分必须在1-5之间");
            }
            // 检查评分是否改变
            if (!review.getRating().equals(oldRating)) {
                ratingChanged = true;
            }
            existingReview.setRating(review.getRating());
        }
        
        if (review.getContent() != null) {
            existingReview.setContent(review.getContent());
        }
        
        if (review.getReplyContent() != null) {
            existingReview.setReplyContent(review.getReplyContent());
            if (review.getReplyTime() != null) {
                existingReview.setReplyTime(review.getReplyTime());
            } else {
                existingReview.setReplyTime(java.time.LocalDateTime.now());
            }
        }
        
        Review updatedReview = reviewRepository.save(existingReview);
        
        // 如果评分改变了，通知评分更新
        if (ratingChanged) {
            if (updatedReview.getMerchant() != null) {
                notifyRatingUpdateForMerchant(updatedReview.getMerchant().getId());
            }
            if (updatedReview.getScenicSpot() != null) {
                notifyRatingUpdateForScenicSpot(updatedReview.getScenicSpot().getId());
            }
        }
        
        return updatedReview;
    }
    
    /**
     * 删除评论
     */
    public void deleteReview(Long id) {
        Optional<Review> opt = reviewRepository.findById(id);
        reviewRepository.markDeletedById(id);
        // 推送评分更新与实时变更
        opt.ifPresent(r -> {
            // 直接推送 review 删除事件，确保商家端实时感知
            if (realtimeDataService != null) {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("action", "review_deleted");
                payload.put("reviewId", id);
                if (r.getMerchant() != null) payload.put("merchantId", r.getMerchant().getId());
                if (r.getScenicSpot() != null) payload.put("scenicSpotId", r.getScenicSpot().getId());
                realtimeDataService.pushReviewUpdate(payload, "delete");
            }
            if (r.getScenicSpot() != null && r.getScenicSpot().getId() != null) {
                notifyRatingUpdateForScenicSpot(r.getScenicSpot().getId());
            }
            if (r.getMerchant() != null && r.getMerchant().getId() != null) {
                notifyRatingUpdateForMerchant(r.getMerchant().getId());
            }
        });
    }

    /**
     * 批量删除评论
     *
     * @param ids 评论ID列表
     */
    public void deleteReviews(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 先取出涉及的景点/商家 ID，用于删除后推送
        java.util.Set<Long> scenicIds = new java.util.HashSet<>();
        java.util.Set<Long> merchantIds = new java.util.HashSet<>();
        for (Long id : ids) {
            reviewRepository.findById(id).ifPresent(r -> {
                if (r.getScenicSpot() != null && r.getScenicSpot().getId() != null) scenicIds.add(r.getScenicSpot().getId());
                if (r.getMerchant() != null && r.getMerchant().getId() != null) merchantIds.add(r.getMerchant().getId());
            });
        }
        reviewRepository.markDeletedByIds(ids);
        // 直接推送 review 批量删除事件
        if (realtimeDataService != null && !ids.isEmpty()) {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("action", "review_deleted");
            payload.put("reviewIds", ids);
            if (!merchantIds.isEmpty()) payload.put("merchantId", merchantIds.iterator().next());
            if (!scenicIds.isEmpty()) payload.put("scenicSpotId", scenicIds.iterator().next());
            realtimeDataService.pushReviewUpdate(payload, "delete");
        }
        // 批量推送评分更新
        for (Long sid : scenicIds) {
            notifyRatingUpdateForScenicSpot(sid);
        }
        for (Long mid : merchantIds) {
            notifyRatingUpdateForMerchant(mid);
        }
    }

    /**
     * 根据景点ID删除所有评论
     */
    public void deleteReviewsByScenicSpotId(Long scenicSpotId) {
        reviewRepository.deleteByScenicSpotId(scenicSpotId);
    }

    /**
     * 根据商家ID删除所有评论
     */
    public void deleteReviewsByMerchantId(Long merchantId) {
        reviewRepository.deleteByMerchantId(merchantId);
    }

    /**
     * 回复评论
     */
    public Review replyReview(Long reviewId, String replyContent) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new RuntimeException("回复内容不能为空");
        }
        review.setReplyContent(replyContent.trim());
        review.setReplyTime(java.time.LocalDateTime.now());
        return reviewRepository.save(review);
    }
    
    /**
     * 获取所有评论
     */
    public List<Review> getAllReviews() {
        return reviewRepository.findAllActive();
    }
    
    /**
     * 创建商家评论
     */
    public Review createMerchantReview(Long userId, Long merchantId, String content, Integer rating) {
        return createMerchantReview(userId, merchantId, content, rating, null);
    }

    /**
     * 创建商家评论（可带图片）
     */
    public Review createMerchantReview(Long userId, Long merchantId, String content, Integer rating, String images) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证商家是否存在
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("商家不存在"));
        
        // 验证评分范围
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }
        
        // 创建评论
        Review review = new Review();
        review.setUser(user);
        review.setMerchant(merchant);
        review.setContent(content);
        review.setRating(rating);
        review.setCreateTime(java.time.LocalDateTime.now());
        if (images != null) {
            review.setImages(images);
        }
        
        return reviewRepository.save(review);
    }
    
    /**
     * 根据商家ID获取评论列表
     */
    public List<Review> getReviewsByMerchantId(Long merchantId) {
        return reviewRepository.findByMerchantId(merchantId);
    }

    /**
     * 商家后台：合并「挂在 merchant_id 上的评价」与「挂在关联景点 scenic_spot 上的评价」
     * （景点类商家用户多在详情页评的是景点，不会出现在仅按 merchant_id 查询的结果中）
     */
    public List<Review> getReviewsForMerchantDashboard(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElse(null);
        if (merchant == null) {
            return List.of();
        }
        LinkedHashMap<Long, Review> byId = new LinkedHashMap<>();
        for (Review r : reviewRepository.findByMerchantId(merchantId)) {
            byId.put(r.getId(), r);
        }
        if (merchant.getScenicSpot() != null && merchant.getScenicSpot().getId() != null) {
            for (Review r : reviewRepository.findByScenicSpotId(merchant.getScenicSpot().getId())) {
                byId.putIfAbsent(r.getId(), r);
            }
        }
        return byId.values().stream()
                .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
                .sorted(Comparator.comparing(Review::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsForMerchantDashboardByRating(Long merchantId, Integer rating) {
        return getReviewsForMerchantDashboard(merchantId).stream()
                .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
                .filter(r -> r.getRating() != null && r.getRating().equals(rating))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据商家ID和评分获取评论列表
     */
    public List<Review> getReviewsByMerchantIdAndRating(Long merchantId, Integer rating) {
        return reviewRepository.findByMerchantIdAndRating(merchantId, rating);
    }
    
    /**
     * 获取商家的平均评分
     */
    public Double getAverageRatingByMerchantId(Long merchantId) {
        Double avgRating = reviewRepository.getAverageRatingByMerchantId(merchantId);
        return avgRating != null ? avgRating : 0.0;
    }
    
    /**
     * 获取商家的评论数量
     */
    public Long getReviewCountByMerchantId(Long merchantId) {
        return reviewRepository.countByMerchantId(merchantId);
    }
    
    /**
     * 通知商家评分更新（评论创建/更新/删除后调用）
     */
    public void notifyRatingUpdateForMerchant(Long merchantId) {
        if (merchantService == null || realtimeDataService == null) {
            return;
        }
        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (merchantOpt.isPresent()) {
                Merchant merchant = merchantOpt.get();
                java.util.Map<String, Object> enrichedData = merchantService.enrichMerchantWithRating(merchant);
                realtimeDataService.pushMerchantUpdate(enrichedData, "update");
                // 同时推送 review 事件，让商家端和游客端评论区实时刷新
                realtimeDataService.pushReviewUpdate(java.util.Map.of("merchantId", merchantId, "action", "review_changed"), "update");
                System.out.println("已推送商家评分更新: Merchant ID=" + merchantId + ", Rating=" + enrichedData.get("rating"));
            }
        } catch (Exception e) {
            System.err.println("推送商家评分更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 通知景点评分更新（评论创建/更新/删除后调用）
     * 同时也会通知关联的商家更新
     */
    public void notifyRatingUpdateForScenicSpot(Long scenicSpotId) {
        if (realtimeDataService == null || merchantService == null) {
            return;
        }
        try {
            // 推送景点更新（评分会在ScenicSpotService中自动计算）
            Optional<ScenicSpot> spotOpt = scenicSpotRepository.findById(scenicSpotId);
            if (spotOpt.isPresent()) {
                ScenicSpot spot = spotOpt.get();
                // 重新计算评分
                Double avgRating = getAverageRatingByScenicSpotId(scenicSpotId);
                if (avgRating != null && avgRating > 0) {
                    spot.setRating(java.math.BigDecimal.valueOf(avgRating).setScale(2, java.math.RoundingMode.HALF_UP));
                } else {
                    spot.setRating(java.math.BigDecimal.ZERO);
                }
                realtimeDataService.pushScenicSpotUpdate(spot, "update");
                // 同时推送 review 事件，让游客端评论区实时刷新
                realtimeDataService.pushReviewUpdate(java.util.Map.of("scenicSpotId", scenicSpotId, "action", "review_changed"), "update");
                System.out.println("已推送景点评分更新: ScenicSpot ID=" + scenicSpotId + ", Rating=" + spot.getRating());
            }
            
            // 查找关联的商家并推送更新
            List<Merchant> merchants = merchantRepository.findAll();
            for (Merchant merchant : merchants) {
                if ("SCENIC".equals(merchant.getCategory()) && 
                    merchant.getScenicSpot() != null && 
                    merchant.getScenicSpot().getId().equals(scenicSpotId)) {
                    java.util.Map<String, Object> enrichedData = merchantService.enrichMerchantWithRating(merchant);
                    realtimeDataService.pushMerchantUpdate(enrichedData, "update");
                    System.out.println("已推送关联商家评分更新: Merchant ID=" + merchant.getId() + ", Rating=" + enrichedData.get("rating"));
                }
            }
        } catch (Exception e) {
            System.err.println("推送景点评分更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}