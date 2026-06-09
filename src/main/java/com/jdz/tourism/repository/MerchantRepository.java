package com.jdz.tourism.repository;

import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    
    /**
     * 根据用户查找商家
     */
    Optional<Merchant> findByUserAndDeletedFalse(User user);
    
    /**
     * 根据用户ID查找商家
     */
    Optional<Merchant> findByUserIdAndDeletedFalse(Long userId);
    
    /**
     * 根据店铺名称查找商家
     */
    Optional<Merchant> findByShopNameAndDeletedFalse(String shopName);
    
    /**
     * 根据分类查找商家
     */
    List<Merchant> findByCategoryAndDeletedFalse(String category);

    /**
     * 根据分类查找已启用的商家（游客端使用）
     */
    List<Merchant> findByCategoryAndDeletedFalseAndEnabledTrue(String category);

    /**
     * 根据景点查找商家（用于景点类型的商家）
     */
    Optional<Merchant> findByScenicSpotAndDeletedFalse(ScenicSpot scenicSpot);

    /**
     * 根据景点ID查找商家（用于景点类型的商家）
     * 使用原生SQL查询，直接通过 scenic_id 字段查找，避免JPA关联加载问题
     * 注意：使用反引号包裹表名，避免MySQL大小写敏感问题
     */
    @Query(value = "SELECT * FROM `merchant` WHERE `scenic_id` = :scenicSpotId AND `deleted` = 0 LIMIT 1", nativeQuery = true)
    Optional<Merchant> findByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);

    /**
     * 根据ID直接删除商家（可配置超时，避免长时间锁等待）
     */
    @Query("SELECT m FROM Merchant m WHERE m.id = :id AND m.deleted = false")
    Optional<Merchant> findActiveById(@Param("id") Long id);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @QueryHints(@QueryHint(name = "jakarta.persistence.query.timeout", value = "1000"))
    @Query("UPDATE Merchant m SET m.deleted = true, m.deletedAt = CURRENT_TIMESTAMP WHERE m.id = :id AND m.deleted = false")
    int markDeletedById(@Param("id") Long id);

    /**
     * 根据用户ID查找商家（不过滤逻辑删除，用于级联物理删除）
     */
    Optional<Merchant> findByUserId(Long userId);

    /**
     * 根据用户ID物理删除商家（用于删除用户时级联清理）
     */
    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Merchant m WHERE m.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}