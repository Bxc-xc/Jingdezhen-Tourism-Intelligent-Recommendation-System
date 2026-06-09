package com.jdz.tourism.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.Reply;
import com.jdz.tourism.entity.Review;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.ScenicSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 统一评论（评价 + 回复树）接口。底层复用 {@code review} 与 {@code reply} 表，
 * 语义上等价于 parent_id + role 的评论模型。
 */
@Service
public class CommentService {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReplyService replyService;

    @Autowired
    private ScenicSpotRepository scenicSpotRepository;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.jdz.tourism.repository.MerchantRepository merchantRepository;

    /**
     * 查询某景点/商家下的全部评价：
     * - 若 scenicId 对应景点存在，同时合并「挂在 scenic_spot_id」和「挂在关联商家 merchant_id」的评价
     * - 否则按商家 ID 查
     * 与商家端 getReviewsForMerchantDashboard 保持一致，避免用户端和商家端数量不一致
     */
    public List<Review> listRootReviewsForScenicOrMerchant(Long scenicId) {
        if (scenicId == null) {
            return List.of();
        }
        if (scenicSpotRepository.existsById(scenicId)) {
            // 合并景点评价 + 关联商家评价（去重）
            java.util.LinkedHashMap<Long, Review> byId = new java.util.LinkedHashMap<>();
            for (Review r : reviewService.getReviewsByScenicSpotId(scenicId)) {
                byId.put(r.getId(), r);
            }
            // 找关联商家，合并其 merchant_id 维度的评价
            merchantRepository.findByScenicSpotId(scenicId).ifPresent(merchant -> {
                for (Review r : reviewService.getReviewsByMerchantId(merchant.getId())) {
                    byId.putIfAbsent(r.getId(), r);
                }
            });
            return byId.values().stream()
                    .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))
                    .sorted(java.util.Comparator.comparing(Review::getCreateTime,
                            java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }
        return reviewService.getReviewsByMerchantId(scenicId);
    }

    public boolean merchantOwnsReview(Merchant merchant, Review review) {
        if (merchant == null || review == null || merchant.getId() == null) {
            return false;
        }
        if (review.getMerchant() != null && merchant.getId().equals(review.getMerchant().getId())) {
            return true;
        }
        if (review.getScenicSpot() != null
                && merchant.getScenicSpot() != null
                && review.getScenicSpot().getId() != null
                && review.getScenicSpot().getId().equals(merchant.getScenicSpot().getId())) {
            return true;
        }
        return false;
    }

    public List<Map<String, Object>> buildCommentThreads(Long scenicId) {
        List<Review> roots = listRootReviewsForScenicOrMerchant(scenicId);
        return roots.stream()
                .map(r -> {
                    Map<String, Object> thread = reviewToMap(r);
                    List<Reply> tree = replyService.getAllRepliesByReviewId(r.getId());
                    thread.put("replies", repliesToMaps(tree));
                    return thread;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> reviewToMap(Review r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("role", "user");
        m.put("parentId", null);
        m.put("content", r.getContent());
        m.put("rating", r.getRating());
        m.put("createTime", r.getCreateTime());
        m.put("images", normalizeReviewImages(r.getImages()));
        if (r.getUser() != null) {
            m.put("user", userToMap(r.getUser()));
            m.put("userId", r.getUser().getId());
        }
        if (r.getScenicSpot() != null) {
            m.put("scenicId", r.getScenicSpot().getId());
        } else {
            m.put("scenicId", null);
        }
        if (r.getMerchant() != null) {
            m.put("merchantId", r.getMerchant().getId());
        } else {
            m.put("merchantId", null);
        }
        return m;
    }

    /**
     * 与前端 toImageArray 一致：支持 JSON 数组字符串、逗号分隔 URL。
     * 仅按逗号切 JSON 会把 URL 截断，导致用户端图片无法展示。
     */
    private List<String> normalizeReviewImages(String images) {
        if (images == null || images.isBlank()) {
            return List.of();
        }
        String s = images.trim();
        if (s.startsWith("[")) {
            try {
                List<String> parsed = objectMapper.readValue(s, new TypeReference<List<String>>() {});
                List<String> out = new ArrayList<>();
                if (parsed != null) {
                    for (String u : parsed) {
                        if (u != null && !u.trim().isEmpty()) {
                            out.add(u.trim());
                        }
                    }
                }
                if (!out.isEmpty()) {
                    return out;
                }
            } catch (Exception ignore) {
            }
        }
        List<String> out = new ArrayList<>();
        for (String part : images.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    private Map<String, Object> userToMap(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("avatar", u.getAvatar());
        return m;
    }

    private Map<String, Object> merchantToMap(Merchant mer) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", mer.getId());
        m.put("shopName", mer.getShopName());
        m.put("avatar", mer.getAvatar());
        return m;
    }

    private List<Map<String, Object>> repliesToMaps(List<Reply> replies) {
        if (replies == null) {
            return List.of();
        }
        return replies.stream().map(this::replyToMap).collect(Collectors.toList());
    }

    private Map<String, Object> replyToMap(Reply rep) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", rep.getId());
        m.put("content", rep.getContent());
        m.put("createTime", rep.getCreateTime());
        boolean isMerchant = rep.getMerchant() != null;
        m.put("role", isMerchant ? "merchant" : "user");
        m.put("parentId", rep.getParentReply() != null ? rep.getParentReply().getId() : null);
        if (rep.getUser() != null) {
            m.put("user", userToMap(rep.getUser()));
        }
        if (rep.getMerchant() != null) {
            m.put("merchant", merchantToMap(rep.getMerchant()));
        }
        m.put("childReplies", repliesToMaps(rep.getChildReplies()));
        return m;
    }

    @Transactional
    public void deleteMerchantReply(Long userId, Long replyId) {
        Optional<Merchant> merOpt = merchantService.getMerchantByUserId(userId);
        if (merOpt.isEmpty()) {
            throw new RuntimeException("当前账号未关联商家");
        }
        Merchant merchant = merOpt.get();
        com.jdz.tourism.entity.Reply reply = replyService.getReplyById(replyId)
                .orElseThrow(() -> new RuntimeException("回复不存在"));
        // 只能删自己商家发的回复
        if (reply.getMerchant() == null || !merchant.getId().equals(reply.getMerchant().getId())) {
            throw new RuntimeException("无权删除该回复");
        }
        replyService.deleteReply(replyId);
    }

    @Transactional
    public Reply merchantReply(Long userId, Long reviewId, Long parentReplyId, String content) {        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        Optional<Merchant> merOpt = merchantService.getMerchantByUserId(userId);
        if (merOpt.isEmpty()) {
            throw new RuntimeException("当前账号未关联商家");
        }
        Merchant merchant = merOpt.get();
        Review review = reviewService.getReviewById(reviewId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        if (!merchantOwnsReview(merchant, review)) {
            throw new RuntimeException("无权回复该评论");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("回复内容不能为空");
        }
        if (parentReplyId == null) {
            return replyService.createMerchantReply(reviewId, merchant.getId(), content.trim());
        }
        return replyService.createMerchantReplyToReply(reviewId, parentReplyId, merchant.getId(), content.trim());
    }
}
