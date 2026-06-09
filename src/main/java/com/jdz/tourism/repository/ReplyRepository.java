package com.jdz.tourism.repository;

import com.jdz.tourism.entity.Reply;
import com.jdz.tourism.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    
    /**
     * 根据评论ID查找所有回复（顶级回复，不包括子回复）
     */
    List<Reply> findByReviewIdAndParentReplyIsNull(Long reviewId);
    
    /**
     * 根据评论查找所有回复（顶级回复，不包括子回复）
     */
    List<Reply> findByReviewAndParentReplyIsNull(Review review);
    
    /**
     * 根据父回复ID查找所有子回复
     */
    List<Reply> findByParentReplyId(Long parentReplyId);
    
    /**
     * 根据父回复查找所有子回复
     */
    List<Reply> findByParentReply(Reply parentReply);
    
    /**
     * 根据评论ID查找所有回复（包括子回复），一次性 JOIN FETCH 关联对象，避免懒加载 N+1
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT r FROM Reply r " +
        "LEFT JOIN FETCH r.user " +
        "LEFT JOIN FETCH r.merchant " +
        "LEFT JOIN FETCH r.parentReply " +
        "WHERE r.review.id = :reviewId"
    )
    List<Reply> findByReviewId(@org.springframework.data.repository.query.Param("reviewId") Long reviewId);
    
    /**
     * 根据用户ID查找所有回复
     */
    List<Reply> findByUserId(Long userId);
    
    /**
     * 根据商家ID查找所有回复
     */
    List<Reply> findByMerchantId(Long merchantId);
}

