package com.jdz.tourism.repository;

import com.jdz.tourism.entity.Activity;
import com.jdz.tourism.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    List<Activity> findByMerchant(Merchant merchant);
    
    List<Activity> findByMerchantId(Long merchantId);
    
    List<Activity> findByMerchantIdAndIsActive(Long merchantId, Boolean isActive);
    
    @Query("SELECT a FROM Activity a WHERE a.merchant.id = :merchantId AND a.isActive = true " +
           "AND (a.endTime IS NULL OR a.endTime >= :now) ORDER BY a.createdAt DESC")
    List<Activity> findActiveActivitiesByMerchant(@Param("merchantId") Long merchantId, @Param("now") LocalDateTime now);
    
    @Query("SELECT a FROM Activity a WHERE a.isActive = true " +
           "AND (a.endTime IS NULL OR a.endTime >= :now) ORDER BY a.createdAt DESC")
    List<Activity> findAllActiveActivities(@Param("now") LocalDateTime now);
}

