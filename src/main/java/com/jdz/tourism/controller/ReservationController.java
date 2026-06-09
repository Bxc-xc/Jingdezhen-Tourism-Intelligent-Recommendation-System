package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Reservation;
import com.jdz.tourism.service.ReservationService;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservation")
@CrossOrigin(origins = "*")
public class ReservationController {
    
    @Autowired
    private ReservationService reservationService;
    
    /**
     * 创建预约
     */
    @LogOperation("创建预约")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReservation(@RequestBody Map<String, Object> reservationData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(reservationData.get("userId").toString());
            Long merchantId = Long.valueOf(reservationData.get("merchantId").toString());
            Long scenicId = Long.valueOf(reservationData.get("scenicId").toString());
            String reservationDate = reservationData.get("reservationDate").toString();
            String reservationTime = reservationData.get("reservationTime").toString();
            Integer participants = Integer.valueOf(reservationData.get("participants").toString());
            String remark = reservationData.getOrDefault("remark", "").toString();
            
            Reservation reservation = reservationService.createReservation(
                userId, merchantId, scenicId, reservationDate, reservationTime, participants, remark
            );
            
            response.put("success", true);
            response.put("message", "预约创建成功");
            response.put("data", reservation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取商家的预约列表
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<Map<String, Object>> getReservationsByMerchant(
            @PathVariable Long merchantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Reservation> reservations = reservationService.getReservationsByMerchant(
                merchantId, status, startDate, endDate
            );
            response.put("success", true);
            response.put("data", reservations);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取用户的预约列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getReservationsByUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Reservation> reservations = reservationService.getReservationsByUser(userId);
            response.put("success", true);
            response.put("data", reservations);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 确认预约
     */
    @LogOperation("确认预约")
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmReservation(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Reservation reservation = reservationService.confirmReservation(id);
            response.put("success", true);
            response.put("message", "预约确认成功");
            response.put("data", reservation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 拒绝预约
     */
    @LogOperation("拒绝预约")
    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectReservation(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Reservation reservation = reservationService.rejectReservation(id);
            response.put("success", true);
            response.put("message", "预约已拒绝");
            response.put("data", reservation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 完成预约
     */
    @LogOperation("完成预约")
    @PutMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeReservation(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Reservation reservation = reservationService.completeReservation(id);
            response.put("success", true);
            response.put("message", "预约已完成");
            response.put("data", reservation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 取消预约
     */
    @LogOperation("取消预约")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelReservation(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Reservation reservation = reservationService.cancelReservation(id);
            response.put("success", true);
            response.put("message", "预约已取消");
            response.put("data", reservation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReservationById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Reservation> reservation = reservationService.getReservationById(id);
            if (reservation.isPresent()) {
                response.put("success", true);
                response.put("data", reservation.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "预约不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取预约统计
     */
    @GetMapping("/merchant/{merchantId}/stats")
    public ResponseEntity<Map<String, Object>> getReservationStats(@PathVariable Long merchantId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = reservationService.getReservationStats(merchantId);
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}