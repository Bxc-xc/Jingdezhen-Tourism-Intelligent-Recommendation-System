package com.jdz.tourism.repository;

import com.jdz.tourism.entity.MerchantFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MerchantFavoriteRepository extends JpaRepository<MerchantFavorite, Long> {
    boolean existsByUserIdAndMerchantId(Long userId, Long merchantId);
    void deleteByUserIdAndMerchantId(Long userId, Long merchantId);
    List<MerchantFavorite> findByUserId(Long userId);

    /** 协同过滤用：获取所有用户-商家收藏对 */
    @Query("SELECT mf.user.id, mf.merchant.id FROM MerchantFavorite mf")
    List<Object[]> findAllUserMerchantPairs();
}
