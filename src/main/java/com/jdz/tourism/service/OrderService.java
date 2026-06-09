package com.jdz.tourism.service;

import com.jdz.tourism.entity.Order;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.repository.OrderRepository;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.ScenicSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    @Autowired
    private ScenicSpotRepository scenicSpotRepository;
    
    @Autowired
    private com.jdz.tourism.repository.RoomTypeRepository roomTypeRepository;

    @Autowired
    private RealtimeDataService realtimeDataService;

    private Map<String, Object> buildOrderRealtimePayload(Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", order.getId());
        payload.put("merchantId", order.getMerchant() != null ? order.getMerchant().getId() : null);
        payload.put("userId", order.getUser() != null ? order.getUser().getId() : null);
        payload.put("scenicSpotId", order.getScenicSpot() != null ? order.getScenicSpot().getId() : null);
        payload.put("orderTime", order.getOrderTime() != null ? order.getOrderTime().toString() : null);
        payload.put("status", order.getStatus() != null ? order.getStatus().name() : null);
        payload.put("reservationTimeSlot", order.getReservationTimeSlot());
        payload.put("reservationNote", order.getReservationNote());
        payload.put("roomTypeName", order.getRoomType() != null ? order.getRoomType().getName() : null);
        payload.put("checkInDate", order.getCheckInDate());
        payload.put("checkOutDate", order.getCheckOutDate());
        payload.put("quantity", order.getQuantity());
        payload.put("totalPrice", order.getTotalPrice());
        return payload;
    }
    
    /**
     * 创建订单
     */
    public Order createOrder(Long userId, Long merchantId, Long scenicSpotId,
                             String reservationTimeSlot, String reservationNote,
                             Long roomTypeId, java.time.LocalDate checkInDate, java.time.LocalDate checkOutDate,
                             Integer quantity, java.math.BigDecimal totalPrice) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证商家是否存在
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("商家不存在"));
        
        // 验证景点是否存在（可选）
        ScenicSpot scenicSpot = null;
        if (scenicSpotId != null) {
            scenicSpot = scenicSpotRepository.findById(scenicSpotId)
                    .orElseThrow(() -> new RuntimeException("景点不存在"));
        }
        
        // 验证房型是否存在（可选）
        com.jdz.tourism.entity.RoomType roomType = null;
        if (roomTypeId != null) {
            roomType = roomTypeRepository.findById(roomTypeId)
                    .orElseThrow(() -> new RuntimeException("房型不存在"));
        }
        
        // 创建订单
        Order order = new Order();
        order.setUser(user);
        order.setMerchant(merchant);
        order.setScenicSpot(scenicSpot);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setReservationTimeSlot(reservationTimeSlot);
        order.setReservationNote(reservationNote);
        order.setRoomType(roomType);
        order.setCheckInDate(checkInDate);
        order.setCheckOutDate(checkOutDate);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);

        Order saved = orderRepository.save(order);
        realtimeDataService.pushOrderUpdate(buildOrderRealtimePayload(saved), "create");
        return saved;
    }
    
    /**
     * 创建订单（旧接口兼容）
     */
    public Order createOrder(Long userId, Long merchantId, Long scenicSpotId,
                             String reservationTimeSlot, String reservationNote) {
        return createOrder(userId, merchantId, scenicSpotId, reservationTimeSlot, reservationNote, null, null, null, null, null);
    }
    
    /**
     * 根据ID获取订单详情
     */
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    /**
     * 根据用户ID获取订单列表
     */
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * 根据商家ID获取订单列表
     */
    public List<Order> getOrdersByMerchantId(Long merchantId) {
        return orderRepository.findByMerchantId(merchantId);
    }
    
    /**
     * 根据景点ID获取订单列表
     */
    public List<Order> getOrdersByScenicSpotId(Long scenicSpotId) {
        return orderRepository.findByScenicSpotId(scenicSpotId);
    }
    
    /**
     * 根据状态获取订单列表
     */
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    /**
     * 更新订单状态
     */
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        realtimeDataService.pushOrderUpdate(buildOrderRealtimePayload(saved), "update");
        return saved;
    }
    
    /**
     * 取消订单
     */
    public Order cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
    }
    
    /**
     * 确认订单
     */
    public Order confirmOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED);
    }
    
    /**
     * 获取所有订单
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    /**
     * 更新订单
     */
    public Order updateOrder(Order order) {
        Order saved = orderRepository.save(order);
        realtimeDataService.pushOrderUpdate(buildOrderRealtimePayload(saved), "update");
        return saved;
    }
    
    /**
     * 删除订单
     */
    public void deleteOrder(Long id) {
        Optional<Order> existing = orderRepository.findById(id);
        orderRepository.deleteById(id);
        existing.ifPresent(order -> {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", order.getId());
            payload.put("merchantId", order.getMerchant() != null ? order.getMerchant().getId() : null);
            realtimeDataService.pushOrderUpdate(payload, "delete");
        });
    }

    /**
     * 根据景点ID删除所有相关订单
     */
    public void deleteOrdersByScenicSpotId(Long scenicSpotId) {
        orderRepository.deleteByScenicSpotId(scenicSpotId);
    }

    /**
     * 根据商家ID删除所有相关订单
     */
    public void deleteOrdersByMerchantId(Long merchantId) {
        orderRepository.deleteByMerchantId(merchantId);
    }
}
