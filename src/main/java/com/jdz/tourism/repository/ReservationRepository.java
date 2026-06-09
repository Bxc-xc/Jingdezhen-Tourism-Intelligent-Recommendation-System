package com.jdz.tourism.repository;

import com.jdz.tourism.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    /**
     * 根据用户ID查找预约列表
     */
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据商家ID查找预约列表
     */
    List<Reservation> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);
    
    /**
     * 根据商家ID和状态查找预约列表
     */
    List<Reservation> findByMerchantIdAndStatusOrderByCreatedAtDesc(Long merchantId, Reservation.ReservationStatus status);
    
    /**
     * 根据商家ID、状态和日期范围查找预约列表
     */
    @Query("SELECT r FROM Reservation r WHERE " +
           "r.merchantId = :merchantId AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:startDate IS NULL OR r.reservationDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.reservationDate <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    List<Reservation> findReservationsByMerchantWithFilters(
        @Param("merchantId") Long merchantId,
        @Param("status") Reservation.ReservationStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * 检查时间冲突
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE " +
           "r.merchantId = :merchantId AND " +
           "r.scenicId = :scenicId AND " +
           "r.reservationDate = :reservationDate AND " +
           "r.reservationTime = :reservationTime AND " +
           "r.status IN ('PENDING', 'CONFIRMED')")
    Long countConflictingReservations(
        @Param("merchantId") Long merchantId,
        @Param("scenicId") Long scenicId,
        @Param("reservationDate") LocalDate reservationDate,
        @Param("reservationTime") LocalTime reservationTime
    );
    
    /**
     * 根据状态统计预约数量
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.merchantId = :merchantId AND r.status = :status")
    Long countByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Reservation.ReservationStatus status);
    
    /**
     * 查找特定日期的预约
     */
    List<Reservation> findByMerchantIdAndReservationDateAndStatusIn(
        Long merchantId, 
        LocalDate reservationDate, 
        List<Reservation.ReservationStatus> statuses
    );
    
    /**
     * 查找即将到来的预约
     */
    @Query("SELECT r FROM Reservation r WHERE " +
           "r.merchantId = :merchantId AND " +
           "r.reservationDate >= :today AND " +
           "r.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY r.reservationDate, r.reservationTime")
    List<Reservation> findUpcomingReservations(
        @Param("merchantId") Long merchantId,
        @Param("today") LocalDate today
    );
}