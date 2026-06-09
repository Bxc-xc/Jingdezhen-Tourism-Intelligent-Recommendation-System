package com.jdz.tourism.repository;

import com.jdz.tourism.entity.Order;
import com.jdz.tourism.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.QueryHint;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 根据用户查找订单
     */
    List<Order> findByUser(User user);
    
    /**
     * 根据用户ID查找订单
     */
    List<Order> findByUserId(Long userId);
    
    /**
     * 根据商家查找订单
     */
    List<Order> findByMerchantId(Long merchantId);

    /**
     * 根据商家ID统计订单数量
     */
    long countByMerchantId(Long merchantId);
    
    /**
     * 根据订单状态查找订单
     */
    List<Order> findByStatus(Order.OrderStatus status);
    
    /**
     * 根据用户和状态查找订单
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.OrderStatus status);

    /**
     * 根据景点ID查找订单
     */
    List<Order> findByScenicSpotId(Long scenicSpotId);

    /**
     * 根据景点ID删除订单
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByScenicSpotId(Long scenicSpotId);

    /**
     * 根据商家ID删除订单
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @QueryHints(@QueryHint(name = "jakarta.persistence.query.timeout", value = "1000"))
    void deleteByMerchantId(Long merchantId);
}