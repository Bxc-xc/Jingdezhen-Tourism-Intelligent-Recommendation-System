package com.jdz.tourism.controller;

import com.jdz.tourism.entity.RoomType;
import com.jdz.tourism.service.RoomTypeService;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    // ==================== 商家端接口 ====================

    /** 商家端：查自己的房型列表 */
    @GetMapping("/api/merchant/room-types/list/{merchantId}")
    public ResponseEntity<Map<String, Object>> getRoomTypes(@PathVariable Long merchantId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<RoomType> roomTypes = roomTypeService.getRoomTypesByMerchantId(merchantId);
            response.put("success", true);
            response.put("data", roomTypes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /** 商家端：新增房型 */
    @LogOperation("创建房型")
    @PostMapping("/api/merchant/room-types/{merchantId}")
    public ResponseEntity<Map<String, Object>> createRoomType(
            @PathVariable Long merchantId,
            @RequestBody RoomType roomType) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomType created = roomTypeService.createRoomType(merchantId, roomType);
            response.put("success", true);
            response.put("message", "房型创建成功");
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /** 商家端 & 管理员端共用：更新房型 */
    @LogOperation("更新房型")
    @PutMapping("/api/merchant/room-types/{id}")
    public ResponseEntity<Map<String, Object>> updateRoomType(
            @PathVariable Long id,
            @RequestBody RoomType roomType) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomType updated = roomTypeService.updateRoomType(id, roomType);
            response.put("success", true);
            response.put("message", "房型更新成功");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /** 商家端 & 管理员端共用：删除房型 */
    @LogOperation("删除房型")
    @DeleteMapping("/api/merchant/room-types/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoomType(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            roomTypeService.deleteRoomType(id);
            response.put("success", true);
            response.put("message", "房型删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== 管理员端接口 ====================

    /** 管理员端：查所有商家的房型 */
    @GetMapping("/api/admin/room-types")
    public ResponseEntity<Map<String, Object>> getAllRoomTypes() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<RoomType> roomTypes = roomTypeService.getAllRoomTypes();
            response.put("success", true);
            response.put("data", roomTypes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /** 管理员端：为指定商家新增房型 */
    @LogOperation("管理员创建房型")
    @PostMapping("/api/admin/room-types/{merchantId}")
    public ResponseEntity<Map<String, Object>> adminCreateRoomType(
            @PathVariable Long merchantId,
            @RequestBody RoomType roomType) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomType created = roomTypeService.createRoomType(merchantId, roomType);
            response.put("success", true);
            response.put("message", "房型创建成功");
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /** 管理员端：更新任意房型 */
    @LogOperation("管理员更新房型")
    @PutMapping("/api/admin/room-types/{id}")
    public ResponseEntity<Map<String, Object>> adminUpdateRoomType(
            @PathVariable Long id,
            @RequestBody RoomType roomType) {
        Map<String, Object> response = new HashMap<>();
        try {
            RoomType updated = roomTypeService.updateRoomType(id, roomType);
            response.put("success", true);
            response.put("message", "房型更新成功");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /** 管理员端：删除任意房型 */
    @LogOperation("管理员删除房型")
    @DeleteMapping("/api/admin/room-types/{id}")
    public ResponseEntity<Map<String, Object>> adminDeleteRoomType(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            roomTypeService.deleteRoomType(id);
            response.put("success", true);
            response.put("message", "房型删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
