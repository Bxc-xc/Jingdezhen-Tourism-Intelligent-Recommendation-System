package com.jdz.tourism.service;

import com.jdz.tourism.entity.Reservation;
import com.jdz.tourism.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {
    
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RealtimeDataService realtimeDataService;

    private Map<String, Object> buildReservationRealtimePayload(Reservation reservation) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", reservation.getId());
        payload.put("userId", reservation.getUserId());
        payload.put("merchantId", reservation.getMerchantId());
        payload.put("scenicId", reservation.getScenicId());
        payload.put("reservationDate", reservation.getReservationDate() != null ? reservation.getReservationDate().toString() : null);
        payload.put("reservationTime", reservation.getReservationTime() != null ? reservation.getReservationTime().toString() : null);
        payload.put("participants", reservation.getParticipants());
        payload.put("remark", reservation.getRemark());
        payload.put("status", reservation.getStatus() != null ? reservation.getStatus().name() : null);
        payload.put("createdAt", reservation.getCreatedAt() != null ? reservation.getCreatedAt().toString() : null);
        payload.put("updatedAt", reservation.getUpdatedAt() != null ? reservation.getUpdatedAt().toString() : null);
        return payload;
    }
    
    /**
     * 创建预约
     */
    public Reservation createReservation(Long userId, Long merchantId, Long scenicId,
                                       String reservationDate, String reservationTime,
                                       Integer participants, String remark) {
        // 解析日期和时间
        LocalDate date = LocalDate.parse(reservationDate);
        LocalTime time = LocalTime.parse(reservationTime);
        
        // 检查时间冲突
        Long conflictCount = reservationRepository.countConflictingReservations(
            merchantId, scenicId, date, time
        );
        
        if (conflictCount > 0) {
            throw new RuntimeException("该时间段已有预约，请选择其他时间");
        }
        
        // 检查预约日期不能是过去的日期
        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("不能预约过去的日期");
        }
        
        Reservation reservation = new Reservation(
            userId, merchantId, scenicId, date, time, participants, remark
        );

        Reservation saved = reservationRepository.save(reservation);
        realtimeDataService.pushReservationUpdate(buildReservationRealtimePayload(saved), "create");
        return saved;
    }
    
    /**
     * 根据商家ID获取预约列表
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByMerchant(Long merchantId, String status,
                                                      String startDate, String endDate) {
        LocalDate start = null;
        LocalDate end = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDate.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDate.parse(endDate);
        }
        
        Reservation.ReservationStatus reservationStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                reservationStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的状态参数: " + status);
            }
        }
        
        return reservationRepository.findReservationsByMerchantWithFilters(
            merchantId, reservationStatus, start, end
        );
    }
    
    /**
     * 根据用户ID获取预约列表
     */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 根据ID获取预约
     */
    @Transactional(readOnly = true)
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }
    
    /**
     * 确认预约
     */
    public Reservation confirmReservation(Long id) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        
        if (!reservationOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Reservation reservation = reservationOpt.get();
        
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("该预约状态不允许确认操作");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setUpdatedAt(java.time.LocalDateTime.now());

        Reservation saved = reservationRepository.save(reservation);
        realtimeDataService.pushReservationUpdate(buildReservationRealtimePayload(saved), "update");
        return saved;
    }
    
    /**
     * 拒绝预约
     */
    public Reservation rejectReservation(Long id) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        
        if (!reservationOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Reservation reservation = reservationOpt.get();
        
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("该预约状态不允许拒绝操作");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(java.time.LocalDateTime.now());

        Reservation saved = reservationRepository.save(reservation);
        realtimeDataService.pushReservationUpdate(buildReservationRealtimePayload(saved), "update");
        return saved;
    }
    
    /**
     * 完成预约
     */
    public Reservation completeReservation(Long id) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        
        if (!reservationOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Reservation reservation = reservationOpt.get();
        
        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("只有已确认的预约才能标记为完成");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservation.setUpdatedAt(java.time.LocalDateTime.now());

        Reservation saved = reservationRepository.save(reservation);
        realtimeDataService.pushReservationUpdate(buildReservationRealtimePayload(saved), "update");
        return saved;
    }
    
    /**
     * 取消预约
     */
    public Reservation cancelReservation(Long id) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        
        if (!reservationOpt.isPresent()) {
            throw new RuntimeException("预约不存在");
        }
        
        Reservation reservation = reservationOpt.get();
        
        if (reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
            throw new RuntimeException("已完成的预约不能取消");
        }
        
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new RuntimeException("该预约已被取消");
        }
        
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(java.time.LocalDateTime.now());

        Reservation saved = reservationRepository.save(reservation);
        realtimeDataService.pushReservationUpdate(buildReservationRealtimePayload(saved), "update");
        return saved;
    }
    
    /**
     * 获取预约统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReservationStats(Long merchantId) {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        stats.put("pending", reservationRepository.countByMerchantIdAndStatus(
            merchantId, Reservation.ReservationStatus.PENDING));
        stats.put("confirmed", reservationRepository.countByMerchantIdAndStatus(
            merchantId, Reservation.ReservationStatus.CONFIRMED));
        stats.put("completed", reservationRepository.countByMerchantIdAndStatus(
            merchantId, Reservation.ReservationStatus.COMPLETED));
        stats.put("cancelled", reservationRepository.countByMerchantIdAndStatus(
            merchantId, Reservation.ReservationStatus.CANCELLED));
        
        return stats;
    }
    
    /**
     * 获取即将到来的预约
     */
    @Transactional(readOnly = true)
    public List<Reservation> getUpcomingReservations(Long merchantId) {
        return reservationRepository.findUpcomingReservations(merchantId, LocalDate.now());
    }
    
    /**
     * 检查时间冲突
     */
    @Transactional(readOnly = true)
    public boolean hasTimeConflict(Long merchantId, Long scenicId, 
                                  String reservationDate, String reservationTime) {
        LocalDate date = LocalDate.parse(reservationDate);
        LocalTime time = LocalTime.parse(reservationTime);
        
        Long conflictCount = reservationRepository.countConflictingReservations(
            merchantId, scenicId, date, time
        );
        
        return conflictCount > 0;
    }
}
