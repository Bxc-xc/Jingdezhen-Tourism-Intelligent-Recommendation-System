package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Order;
import com.jdz.tourism.service.OrderService;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     */
    @LogOperation("创建订单")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(orderData.get("userId").toString());
            Long merchantId = Long.valueOf(orderData.get("merchantId").toString());
            Long scenicSpotId = null;
            if (orderData.get("scenicSpotId") != null) {
                scenicSpotId = Long.valueOf(orderData.get("scenicSpotId").toString());
            }

            String reservationTimeSlot = orderData.get("reservationTimeSlot") != null
                    ? orderData.get("reservationTimeSlot").toString()
                    : null;
            String reservationNote = orderData.get("reservationNote") != null
                    ? orderData.get("reservationNote").toString()
                    : null;
            
            Long roomTypeId = orderData.get("roomTypeId") != null
                    ? Long.valueOf(orderData.get("roomTypeId").toString())
                    : null;
            
            java.time.LocalDate checkInDate = orderData.get("checkInDate") != null
                    ? java.time.LocalDate.parse(orderData.get("checkInDate").toString())
                    : null;
            
            java.time.LocalDate checkOutDate = orderData.get("checkOutDate") != null
                    ? java.time.LocalDate.parse(orderData.get("checkOutDate").toString())
                    : null;
            
            Integer quantity = orderData.get("quantity") != null
                    ? Integer.valueOf(orderData.get("quantity").toString())
                    : null;
            
            java.math.BigDecimal totalPrice = orderData.get("totalPrice") != null
                    ? new java.math.BigDecimal(orderData.get("totalPrice").toString())
                    : null;

            Order order = orderService.createOrder(userId, merchantId, scenicSpotId, reservationTimeSlot, reservationNote,
                    roomTypeId, checkInDate, checkOutDate, quantity, totalPrice);
            response.put("success", true);
            response.put("message", "订单创建成功");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据ID获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Order> order = orderService.getOrderById(id);
            if (order.isPresent()) {
                response.put("success", true);
                response.put("data", order.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "订单不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据用户ID获取订单列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getOrdersByUserId(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            response.put("success", true);
            response.put("data", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据商家ID获取订单列表
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<Map<String, Object>> getOrdersByMerchantId(@PathVariable Long merchantId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Order> orders = orderService.getOrdersByMerchantId(merchantId);
            response.put("success", true);
            response.put("data", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据状态获取订单列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            response.put("success", true);
            response.put("data", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新订单状态
     */
    @LogOperation("更新订单状态")
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String status = statusData.get("status");
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            
            Order order = orderService.updateOrderStatus(id, orderStatus);
            response.put("success", true);
            response.put("message", "订单状态更新成功");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 取消订单
     */
    @LogOperation("取消订单")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order order = orderService.cancelOrder(id);
            response.put("success", true);
            response.put("message", "订单取消成功");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 确认订单
     */
    @LogOperation("确认订单")
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order order = orderService.confirmOrder(id);
            response.put("success", true);
            response.put("message", "订单确认成功");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取所有订单
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Order> orders = orderService.getAllOrders();
            response.put("success", true);
            response.put("data", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新订单
     */
    @LogOperation("更新订单信息")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> orderData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取现有订单
            Optional<Order> existingOrderOpt = orderService.getOrderById(id);
            if (!existingOrderOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "订单不存在");
                return ResponseEntity.notFound().build();
            }
            
            Order existingOrder = existingOrderOpt.get();
            
            // 更新状态（如果提供）
            if (orderData.containsKey("status")) {
                String statusStr = orderData.get("status").toString();
                Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
                existingOrder.setStatus(status);
            }
            
            // 保存更新后的订单
            Order updatedOrder = orderService.updateOrder(existingOrder);
            response.put("success", true);
            response.put("message", "订单更新成功");
            response.put("data", updatedOrder);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除订单
     */
    @LogOperation("删除订单")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            orderService.deleteOrder(id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}