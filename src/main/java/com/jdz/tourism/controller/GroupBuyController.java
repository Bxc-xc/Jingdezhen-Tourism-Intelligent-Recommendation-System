package com.jdz.tourism.controller;

import com.jdz.tourism.entity.GroupBuy;
import com.jdz.tourism.entity.GroupBuyOrder;
import com.jdz.tourism.service.GroupBuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group-buy")
@CrossOrigin(origins = "*")
public class GroupBuyController {

    @Autowired
    private GroupBuyService groupBuyService;

    // ===== 用户端 =====

    /** 获取某商家已审核通过的团购列表 */
    @GetMapping("/merchant/{merchantId}/public")
    public ResponseEntity<Map<String, Object>> getPublicList(@PathVariable Long merchantId) {
        Map<String, Object> res = new HashMap<>();
        try {
            List<GroupBuy> list = groupBuyService.getApprovedGroupBuys(merchantId);
            res.put("success", true);
            res.put("data", list.stream().map(this::buildGroupBuyVO).collect(java.util.stream.Collectors.toList()));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** 用户下单 */
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody Map<String, Object> data) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = Long.valueOf(data.get("userId").toString());
            GroupBuyOrder order = groupBuyService.placeOrder(userId, data);
            res.put("success", true);
            res.put("message", "购买成功");
            res.put("data", buildOrderVO(order));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** 用户查看自己的团购订单 */
    @GetMapping("/order/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrders(@PathVariable Long userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            List<GroupBuyOrder> orders = groupBuyService.getUserOrders(userId);
            res.put("success", true);
            res.put("data", orders.stream().map(this::buildOrderVO).toList());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // ===== 商家端 =====

    /** 商家发布团购 */
    @PostMapping("/merchant/{merchantId}")
    public ResponseEntity<Map<String, Object>> create(@PathVariable Long merchantId,
                                                       @RequestBody Map<String, Object> data) {
        Map<String, Object> res = new HashMap<>();
        try {
            GroupBuy gb = groupBuyService.createGroupBuy(merchantId, data);
            res.put("success", true);
            res.put("message", "发布成功，等待审核");
            res.put("data", buildGroupBuyVO(gb));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** 商家编辑团购 */
    @PutMapping("/merchant/{merchantId}/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long merchantId,
                                                       @PathVariable Long id,
                                                       @RequestBody Map<String, Object> data) {
        Map<String, Object> res = new HashMap<>();
        try {
            GroupBuy gb = groupBuyService.updateGroupBuy(id, merchantId, data);
            res.put("success", true);
            res.put("message", "修改成功，等待重新审核");
            res.put("data", buildGroupBuyVO(gb));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** 商家删除团购 */
    @DeleteMapping("/merchant/{merchantId}/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long merchantId,
                                                       @PathVariable Long id) {
        Map<String, Object> res = new HashMap<>();
        try {
            groupBuyService.deleteGroupBuy(id, merchantId);
            res.put("success", true);
            res.put("message", "删除成功");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** 商家查看自己的团购列表 */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<Map<String, Object>> getMerchantList(@PathVariable Long merchantId) {
        Map<String, Object> res = new HashMap<>();
        try {
            List<GroupBuy> list = groupBuyService.getMerchantGroupBuys(merchantId);
            res.put("success", true);
            res.put("data", list.stream().map(this::buildGroupBuyVO).toList());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** 商家查看团购订单 */
    @GetMapping("/order/merchant/{merchantId}")
    public ResponseEntity<Map<String, Object>> getMerchantOrders(@PathVariable Long merchantId) {
        Map<String, Object> res = new HashMap<>();
        try {
            List<GroupBuyOrder> orders = groupBuyService.getMerchantOrders(merchantId);
            res.put("success", true);
            res.put("data", orders.stream().map(this::buildOrderVO).toList());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // ===== 管理员端 =====

    /** 管理员查看所有团购 */
    @GetMapping("/admin/list")
    public ResponseEntity<Map<String, Object>> adminList() {
        Map<String, Object> res = new HashMap<>();
        try {
            List<GroupBuy> list = groupBuyService.getAllGroupBuys();
            res.put("success", true);
            res.put("data", list.stream().map(this::buildGroupBuyVO).toList());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** 管理员审核团购 */
    @PutMapping("/admin/{id}/audit")
    public ResponseEntity<Map<String, Object>> audit(@PathVariable Long id,
                                                      @RequestBody Map<String, Object> data) {
        Map<String, Object> res = new HashMap<>();
        try {
            String status = data.get("status").toString();
            String rejectReason = data.get("rejectReason") != null ? data.get("rejectReason").toString() : null;
            GroupBuy gb = groupBuyService.auditGroupBuy(id, status, rejectReason);
            res.put("success", true);
            res.put("message", "操作成功");
            res.put("data", buildGroupBuyVO(gb));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** 管理员修改团购 */
    @PutMapping("/admin/{id}")
    public ResponseEntity<Map<String, Object>> adminUpdate(@PathVariable Long id,
                                                            @RequestBody Map<String, Object> data) {
        Map<String, Object> res = new HashMap<>();
        try {
            GroupBuy gb = groupBuyService.adminUpdateGroupBuy(id, data);
            res.put("success", true);
            res.put("message", "修改成功");
            res.put("data", buildGroupBuyVO(gb));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    /** 管理员删除团购 */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, Object>> adminDelete(@PathVariable Long id) {
        Map<String, Object> res = new HashMap<>();
        try {
            groupBuyService.adminDeleteGroupBuy(id);
            res.put("success", true);
            res.put("message", "删除成功");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // ===== VO 构建 =====

    private Map<String, Object> buildGroupBuyVO(GroupBuy gb) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", gb.getId());
        vo.put("merchantId", gb.getMerchant() != null ? gb.getMerchant().getId() : null);
        vo.put("merchantName", gb.getMerchant() != null ? gb.getMerchant().getShopName() : null);
        vo.put("name", gb.getName());
        vo.put("image", gb.getImage());
        vo.put("groupPrice", gb.getGroupPrice());
        vo.put("originalPrice", gb.getOriginalPrice());
        vo.put("stock", gb.getStock());
        vo.put("soldCount", gb.getSoldCount());
        vo.put("usageDesc", gb.getUsageDesc());
        vo.put("detail", gb.getDetail());
        vo.put("validStart", gb.getValidStart());
        vo.put("validEnd", gb.getValidEnd());
        vo.put("status", gb.getStatus());
        vo.put("rejectReason", gb.getRejectReason());
        vo.put("createdAt", gb.getCreatedAt());
        return vo;
    }

    private Map<String, Object> buildOrderVO(GroupBuyOrder o) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", o.getId());
        vo.put("groupBuyId", o.getGroupBuy() != null ? o.getGroupBuy().getId() : null);
        vo.put("groupBuyName", o.getGroupBuy() != null ? o.getGroupBuy().getName() : null);
        vo.put("groupBuyImage", o.getGroupBuy() != null ? o.getGroupBuy().getImage() : null);
        vo.put("groupPrice", o.getGroupBuy() != null ? o.getGroupBuy().getGroupPrice() : null);
        vo.put("merchantId", o.getMerchant() != null ? o.getMerchant().getId() : null);
        vo.put("merchantName", o.getMerchant() != null ? o.getMerchant().getShopName() : null);
        vo.put("userId", o.getUser() != null ? o.getUser().getId() : null);
        vo.put("quantity", o.getQuantity());
        vo.put("totalPrice", o.getTotalPrice());
        vo.put("useDate", o.getUseDate());
        vo.put("contactName", o.getContactName());
        vo.put("contactPhone", o.getContactPhone());
        vo.put("status", o.getStatus());
        vo.put("createdAt", o.getCreatedAt());
        return vo;
    }
}
