package com.jdz.tourism.repository;

import com.jdz.tourism.entity.ScenicSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScenicSpotRepository extends JpaRepository<ScenicSpot, Long> {
    
    /**
     * 根据分类查找景点
     */
    List<ScenicSpot> findByCategory(String category);
    
    /**
     * 根据标签查找景点（模糊匹配，忽略大小写）
     */
    @Query("SELECT s FROM ScenicSpot s WHERE LOWER(s.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<ScenicSpot> findByTagsContaining(@Param("tag") String tag);
    
    /**
     * 根据单个标签查找景点（用于多标签搜索）
     * 注意：tags字段是逗号分隔的字符串，不能直接使用IN查询
     * 在Service层循环调用此方法或使用findByTagsContaining
     */
    @Query("SELECT s FROM ScenicSpot s WHERE s.tags LIKE CONCAT('%', :tag, '%')")
    List<ScenicSpot> findByTagsIn(@Param("tag") String tag);
    
    /**
     * 按访问量降序查找热门景点
     */
    List<ScenicSpot> findTop10ByOrderByVisitCountDesc();
    
    /**
     * 按访问量降序查找前8个热门景点
     */
    List<ScenicSpot> findTop8ByOrderByVisitCountDesc();
    
    /**
     * 根据名称模糊查找景点
     */
    List<ScenicSpot> findByNameContainingIgnoreCase(String name);
    
    /**
     * 根据名称精确查找景点
     */
    java.util.Optional<ScenicSpot> findByName(String name);
    
    /**
     * 根据价格范围查找景点（BigDecimal避免精度问题）
     */
    List<ScenicSpot> findByPriceBetween(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
    
    /**
     * 综合搜索：名称、描述、标签
     */
    @Query("SELECT s FROM ScenicSpot s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ScenicSpot> findByKeywordInNameOrDescriptionOrTags(@Param("keyword") String keyword);

    /**
     * 综合搜索（分页）：名称、描述、标签
     */
    @Query("SELECT s FROM ScenicSpot s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ScenicSpot> findByKeywordInNameOrDescriptionOrTags(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据描述模糊查找景点
     */
    @Query("SELECT s FROM ScenicSpot s WHERE LOWER(s.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<ScenicSpot> findByDescriptionContainingIgnoreCase(@Param("description") String description);
}