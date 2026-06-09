package com.jdz.tourism.repository;

import com.jdz.tourism.entity.MerchantActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.QueryHint;
import java.util.List;

@Repository
public interface MerchantActivityRepository extends JpaRepository<MerchantActivity, Long> {
    
    /**
     * 获取商家的所有活动，按开始时间倒序排列
     */
    List<MerchantActivity> findByMerchantIdOrderByStartTimeDesc(Long merchantId);
    
    /**
     * 获取商家的所有活动
     */
    List<MerchantActivity> findByMerchantId(Long merchantId);

    /**
     * 判断商家是否仍有关联活动
     */
    boolean existsByMerchantId(Long merchantId);

    /**
     * 根据商家ID直接删除活动（避免先查后删带来的锁持有时间）
     */
    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @QueryHints(@QueryHint(name = "jakarta.persistence.query.timeout", value = "1000"))
    @Query("DELETE FROM MerchantActivity a WHERE a.merchant.id = :merchantId")
    int deleteByMerchantId(@Param("merchantId") Long merchantId);
}
