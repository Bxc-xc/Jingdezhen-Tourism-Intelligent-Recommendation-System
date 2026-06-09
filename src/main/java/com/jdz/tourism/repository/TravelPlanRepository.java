package com.jdz.tourism.repository;

import com.jdz.tourism.entity.TravelPlan;
import com.jdz.tourism.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    
    /**
     * 根据用户查找行程列表
     */
    List<TravelPlan> findByUser(User user);
    
    /**
     * 根据用户ID查找行程列表
     */
    List<TravelPlan> findByUserId(Long userId);
    
    /**
     * 根据用户ID查找行程列表，按创建时间倒序
     */
    List<TravelPlan> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    /**
     * 根据用户ID和标题模糊查找行程
     */
    @Query("SELECT tp FROM TravelPlan tp WHERE tp.user.id = :userId AND tp.title LIKE %:keyword%")
    List<TravelPlan> findByUserIdAndTitleContaining(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    /**
     * 根据预算类型查找行程
     */
    List<TravelPlan> findByUserIdAndBudgetType(Long userId, String budgetType);
    
    /**
     * 根据天数范围查找行程
     */
    @Query("SELECT tp FROM TravelPlan tp WHERE tp.user.id = :userId AND tp.days BETWEEN :minDays AND :maxDays")
    List<TravelPlan> findByUserIdAndDaysBetween(@Param("userId") Long userId, 
                                               @Param("minDays") Integer minDays, 
                                               @Param("maxDays") Integer maxDays);
}