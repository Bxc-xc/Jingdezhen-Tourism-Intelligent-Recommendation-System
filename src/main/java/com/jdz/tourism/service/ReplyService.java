package com.jdz.tourism.service;

import com.jdz.tourism.entity.Reply;
import com.jdz.tourism.entity.Review;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.repository.ReplyRepository;
import com.jdz.tourism.repository.ReviewRepository;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ReplyService {
    
    @Autowired
    private ReplyRepository replyRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    /**
     * 创建回复（游客回复评论）
     */
    @Transactional
    public Reply createUserReply(Long reviewId, Long userId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("回复内容不能为空");
        }
        
        Reply reply = new Reply(review, user, content);
        return replyRepository.save(reply);
    }
    
    /**
     * 创建回复（商家回复评论）
     */
    @Transactional
    public Reply createMerchantReply(Long reviewId, Long merchantId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("商家不存在"));
        
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("回复内容不能为空");
        }
        
        Reply reply = new Reply(review, merchant, content);
        return replyRepository.save(reply);
    }
    
    /**
     * 创建回复的回复（游客回复回复）
     */
    @Transactional
    public Reply createUserReplyToReply(Long reviewId, Long parentReplyId, Long userId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        Reply parentReply = replyRepository.findById(parentReplyId)
                .orElseThrow(() -> new RuntimeException("父回复不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("回复内容不能为空");
        }
        
        Reply reply = new Reply(review, user, parentReply, content);
        return replyRepository.save(reply);
    }
    
    /**
     * 创建回复的回复（商家回复回复）
     */
    @Transactional
    public Reply createMerchantReplyToReply(Long reviewId, Long parentReplyId, Long merchantId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        Reply parentReply = replyRepository.findById(parentReplyId)
                .orElseThrow(() -> new RuntimeException("父回复不存在"));
        
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("商家不存在"));
        
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("回复内容不能为空");
        }
        
        Reply reply = new Reply(review, merchant, parentReply, content);
        return replyRepository.save(reply);
    }
    
    /**
     * 根据评论ID获取所有顶级回复（不包括子回复）
     */
    public List<Reply> getTopLevelRepliesByReviewId(Long reviewId) {
        return replyRepository.findByReviewIdAndParentReplyIsNull(reviewId);
    }
    
    /**
     * 根据父回复ID获取所有子回复
     */
    public List<Reply> getChildRepliesByParentId(Long parentReplyId) {
        return replyRepository.findByParentReplyId(parentReplyId);
    }
    
    /**
     * 根据评论ID获取所有回复（包括子回复，以树形结构返回）
     * 一次性查出所有回复，在内存中组装树，避免 N+1 查询
     */
    @Transactional(readOnly = true)
    public List<Reply> getAllRepliesByReviewId(Long reviewId) {
        // 一次查出该评论下全部回复
        List<Reply> all = replyRepository.findByReviewId(reviewId);
        // 按 id 建索引
        java.util.Map<Long, Reply> byId = new java.util.LinkedHashMap<>();
        for (Reply r : all) {
            r.setChildReplies(new java.util.ArrayList<>());
            byId.put(r.getId(), r);
        }
        // 组装树
        List<Reply> roots = new java.util.ArrayList<>();
        for (Reply r : all) {
            if (r.getParentReply() == null) {
                roots.add(r);
            } else {
                Reply parent = byId.get(r.getParentReply().getId());
                if (parent != null) {
                    parent.getChildReplies().add(r);
                } else {
                    roots.add(r); // 父节点不存在时作为顶级
                }
            }
        }
        return roots;
    }
    
    /**
     * 根据ID获取回复
     */
    public Optional<Reply> getReplyById(Long id) {
        return replyRepository.findById(id);
    }
    
    /**
     * 删除回复
     */
    @Transactional
    public void deleteReply(Long id) {
        replyRepository.deleteById(id);
    }
    
    /**
     * 更新回复
     */
    @Transactional
    public Reply updateReply(Long id, String content) {
        Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("回复不存在"));
        
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("回复内容不能为空");
        }
        
        reply.setContent(content.trim());
        return replyRepository.save(reply);
    }
}

