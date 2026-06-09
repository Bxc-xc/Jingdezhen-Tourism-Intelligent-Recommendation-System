package com.jdz.tourism.repository;

import com.jdz.tourism.entity.Review;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.QueryHint;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * 根据景点查找评论
     */
    @Query("SELECT r FROM Review r WHERE r.scenicSpot = :scenicSpot AND (r.deleted = false OR r.deleted IS NULL)")
    List<Review> findByScenicSpot(@Param("scenicSpot") ScenicSpot scenicSpot);
    
    /**
     * 根据景点ID查找评论
     */
    @Query("SELECT r FROM Review r WHERE r.scenicSpot.id = :scenicSpotId AND (r.deleted = false OR r.deleted IS NULL)")
    List<Review> findByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);
    
    /**
     * 根据用户查找评论
     */
    @Query("SELECT r FROM Review r WHERE r.user = :user AND (r.deleted = false OR r.deleted IS NULL)")
    List<Review> findByUser(@Param("user") User user);
    
    /**
     * 根据用户ID查找评论
     */
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND (r.deleted = false OR r.deleted IS NULL)")
    List<Review> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据景点ID和评分查找评论
     */
    @Query("SELECT r FROM Review r WHERE r.scenicSpot.id = :scenicSpotId AND r.rating = :rating AND (r.deleted = false OR r.deleted IS NULL)")
    List<Review> findByScenicSpotIdAndRating(@Param("scenicSpotId") Long scenicSpotId, @Param("rating") Integer rating);
    
    /**
     * 计算景点的平均评分
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.scenicSpot.id = :scenicSpotId AND (r.deleted = false OR r.deleted IS NULL)")
    Double getAverageRatingByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);
    
    /**
     * 根据景点ID统计评论数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.scenicSpot.id = :scenicSpotId AND (r.deleted = false OR r.deleted IS NULL)")
    Long countByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);
    
    /**
     * 根据商家查找评论
     */
    @Query("SELECT r FROM Review r WHERE r.merchant = :merchant AND (r.deleted = false OR r.deleted IS NULL)")
    List<Review> findByMerchant(@Param("merchant") Merchant merchant);
    
    /**
     * 根据商家ID查找评论
     */
    @Query("SELECT r FROM Review r WHERE r.merchant.id = :merchantId AND (r.deleted = false OR r.deleted IS NULL)")
    List<Review> findByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID和评分查找评论
     */
    @Query("SELECT r FROM Review r WHERE r.merchant.id = :merchantId AND r.rating = :rating AND (r.deleted = false OR r.deleted IS NULL)")
    List<Review> findByMerchantIdAndRating(@Param("merchantId") Long merchantId, @Param("rating") Integer rating);
    
    /**
     * 计算商家的平均评分
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.merchant.id = :merchantId AND (r.deleted = false OR r.deleted IS NULL)")
    Double getAverageRatingByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID统计评论数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.merchant.id = :merchantId AND (r.deleted = false OR r.deleted IS NULL)")
    Long countByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 判断商家是否仍有关联评论
     */
    @Query("SELECT CASE WHEN COUNT(r)>0 THEN true ELSE false END FROM Review r WHERE r.merchant.id = :merchantId AND (r.deleted = false OR r.deleted IS NULL)")
    boolean existsByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 根据景点ID删除评论
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE Review r SET r.deleted = true, r.deletedAt = CURRENT_TIMESTAMP WHERE r.scenicSpot.id = :scenicSpotId AND (r.deleted = false OR r.deleted IS NULL)")
    void deleteByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);

    /**
     * 根据商家ID删除评论
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @QueryHints(@QueryHint(name = "jakarta.persistence.query.timeout", value = "1000"))
    @Query("UPDATE Review r SET r.deleted = true, r.deletedAt = CURRENT_TIMESTAMP WHERE r.merchant.id = :merchantId AND (r.deleted = false OR r.deleted IS NULL)")
    void deleteByMerchantId(@Param("merchantId") Long merchantId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE Review r SET r.deleted = true, r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :id AND (r.deleted = false OR r.deleted IS NULL)")
    int markDeletedById(@Param("id") Long id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE Review r SET r.deleted = true, r.deletedAt = CURRENT_TIMESTAMP WHERE r.id IN :ids AND (r.deleted = false OR r.deleted IS NULL)")
    int markDeletedByIds(@Param("ids") List<Long> ids);

    // 获取未删除的全部评论（管理员列表用）
    @Query("SELECT r FROM Review r WHERE (r.deleted = false OR r.deleted IS NULL) ORDER BY r.createTime DESC")
    List<Review> findAllActive();
}