package com.jdz.tourism.repository;

import com.jdz.tourism.entity.MerchantApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantApplicationRepository extends JpaRepository<MerchantApplication, Long> {
    
    /**
     * 根据用户ID查找申请
     */
    Optional<MerchantApplication> findByUserId(Long userId);
    
    /**
     * 根据用户ID查找最新的申请（按创建时间降序）
     */
    @Query("SELECT a FROM MerchantApplication a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    java.util.List<MerchantApplication> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * 查找用户是否有非拒绝状态的申请
     */
    @Query("SELECT COUNT(a) > 0 FROM MerchantApplication a WHERE a.userId = :userId AND a.status != 'REJECTED'")
    boolean existsNonRejectedApplicationByUserId(@Param("userId") Long userId);
    
    /**
     * 根据状态查找申请列表
     */
    List<MerchantApplication> findByStatus(MerchantApplication.ApplicationStatus status);
    
    /**
     * 根据用户ID和状态查找申请
     */
    Optional<MerchantApplication> findByUserIdAndStatus(Long userId, MerchantApplication.ApplicationStatus status);
    
    /**
     * 查找待审核的申请数量
     */
    @Query("SELECT COUNT(a) FROM MerchantApplication a WHERE a.status = 'PENDING'")
    Long countPendingApplications();
    
    /**
     * 查找已通过的申请数量
     */
    @Query("SELECT COUNT(a) FROM MerchantApplication a WHERE a.status = 'APPROVED'")
    Long countApprovedApplications();
    
    /**
     * 查找已拒绝的申请数量
     */
    @Query("SELECT COUNT(a) FROM MerchantApplication a WHERE a.status = 'REJECTED'")
    Long countRejectedApplications();
    
    /**
     * 根据状态和日期范围查找申请
     */
    @Query("SELECT a FROM MerchantApplication a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    List<MerchantApplication> findApplicationsWithFilters(
        @Param("status") MerchantApplication.ApplicationStatus status,
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );
}