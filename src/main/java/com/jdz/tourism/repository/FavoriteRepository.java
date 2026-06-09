package com.jdz.tourism.repository;

import com.jdz.tourism.entity.Favorite;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.entity.ScenicSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    /**
     * 根据用户查找收藏列表
     */
    List<Favorite> findByUser(User user);
    
    /**
     * 根据用户ID查找收藏列表
     */
    List<Favorite> findByUserId(Long userId);
    
    /**
     * 根据用户和景点查找收藏
     */
    Optional<Favorite> findByUserAndScenicSpot(User user, ScenicSpot scenicSpot);
    
    /**
     * 根据用户ID和景点ID查找收藏
     */
    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId AND f.scenicSpot.id = :scenicSpotId")
    Optional<Favorite> findByUserIdAndScenicSpotId(@Param("userId") Long userId, @Param("scenicSpotId") Long scenicSpotId);
    
    /**
     * 检查用户是否收藏了某个景点
     */
    boolean existsByUserIdAndScenicSpotId(Long userId, Long scenicSpotId);
    
    /**
     * 根据用户ID和景点ID删除收藏
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.scenicSpot.id = :scenicSpotId")
    void deleteByUserIdAndScenicSpotId(@Param("userId") Long userId, @Param("scenicSpotId") Long scenicSpotId);

    /**
     * 根据景点ID删除所有收藏记录
     */
    @Modifying
    @Transactional
    void deleteByScenicSpotId(Long scenicSpotId);

    /**
     * 查询所有收藏记录（协同过滤用）
     * 只取 userId 和 scenicSpotId，避免加载全部关联对象
     */
    @Query("SELECT f.user.id, f.scenicSpot.id FROM Favorite f WHERE f.scenicSpot IS NOT NULL")
    List<Object[]> findAllUserScenicPairs();

    /**
     * 查询某个景点被收藏的总次数
     */
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.scenicSpot.id = :scenicSpotId")
    long countByScenicSpotId(@Param("scenicSpotId") Long scenicSpotId);
}