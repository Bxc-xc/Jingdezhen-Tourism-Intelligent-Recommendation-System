package com.jdz.tourism.service;

import com.jdz.tourism.entity.GroupBuy;
import com.jdz.tourism.entity.GroupBuyOrder;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.GroupBuyOrderRepository;
import com.jdz.tourism.repository.GroupBuyRepository;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class GroupBuyService {

    @Autowired
    private GroupBuyRepository groupBuyRepository;

    @Autowired
    private GroupBuyOrderRepository groupBuyOrderRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private RealtimeDataService realtimeDataService;

    // ---- 商家端 ----

    public GroupBuy createGroupBuy(Long merchantId, Map<String, Object> data) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("商家不存在"));
        GroupBuy gb = buildFromMap(new GroupBuy(), data);
        gb.setMerchant(merchant);
        gb.setStatus("PENDING");
        gb.setSoldCount(0);
        return groupBuyRepository.save(gb);
    }

    public GroupBuy updateGroupBuy(Long id, Long merchantId, Map<String, Object> data) {
        GroupBuy gb = groupBuyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("团购不存在"));
        if (!gb.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException("无权操作");
        }
        buildFromMap(gb, data);
        gb.setStatus("PENDING"); // 修改后重新审核
        return groupBuyRepository.save(gb);
    }

    public void deleteGroupBuy(Long id, Long merchantId) {
        GroupBuy gb = groupBuyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("团购不存在"));
        if (!gb.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException("无权操作");
        }
        groupBuyRepository.delete(gb);
    }

    public List<GroupBuy> getMerchantGroupBuys(Long merchantId) {
        return groupBuyRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    // ---- 用户端 ----

    public List<GroupBuy> getApprovedGroupBuys(Long merchantId) {
        return groupBuyRepository.findApprovedWithMerchant(merchantId, "APPROVED");
    }

    @Transactional
    public GroupBuyOrder placeOrder(Long userId, Map<String, Object> data) {
        Long groupBuyId = Long.valueOf(data.get("groupBuyId").toString());
        int qty = Integer.parseInt(data.get("quantity").toString());

        GroupBuy gb = groupBuyRepository.findById(groupBuyId)
                .orElseThrow(() -> new RuntimeException("团购不存在"));

        if (!"APPROVED".equals(gb.getStatus())) {
            throw new RuntimeException("该团购暂不可购买");
        }
        if (gb.getStock() < qty) {
            throw new RuntimeException("库存不足，当前剩余：" + gb.getStock());
        }
        LocalDate validEnd = gb.getValidEnd();
        if (validEnd != null && LocalDate.now().isAfter(validEnd)) {
            throw new RuntimeException("该团购已过期");
        }

        int updated = groupBuyRepository.decreaseStock(groupBuyId, qty);
        if (updated == 0) {
            throw new RuntimeException("库存不足，请刷新后重试");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        GroupBuyOrder order = new GroupBuyOrder();
        order.setGroupBuy(gb);
        order.setUser(user);
        order.setMerchant(gb.getMerchant());
        order.setQuantity(qty);
        order.setTotalPrice(gb.getGroupPrice().multiply(BigDecimal.valueOf(qty)));
        order.setUseDate(LocalDate.parse(data.get("useDate").toString()));
        order.setContactName(data.get("contactName").toString());
        order.setContactPhone(data.get("contactPhone").toString());
        order.setStatus("PAID");

        GroupBuyOrder saved = groupBuyOrderRepository.save(order);

        // 推送团购订单事件，通知商家端实时刷新
        if (realtimeDataService != null) {
            realtimeDataService.pushOrderUpdate(java.util.Map.of(
                "type", "group_buy_order",
                "merchantId", gb.getMerchant().getId(),
                "orderId", saved.getId(),
                "groupBuyName", gb.getName()
            ), "create");
        }

        return saved;
    }

    public List<GroupBuyOrder> getUserOrders(Long userId) {
        return groupBuyOrderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ---- 管理员端 ----

    public List<GroupBuy> getAllGroupBuys() {
        return groupBuyRepository.findAllByOrderByCreatedAtDesc();
    }

    public GroupBuy auditGroupBuy(Long id, String status, String rejectReason) {
        GroupBuy gb = groupBuyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("团购不存在"));
        gb.setStatus(status);
        if ("REJECTED".equals(status) && rejectReason != null) {
            gb.setRejectReason(rejectReason);
        }
        return groupBuyRepository.save(gb);
    }

    public void adminDeleteGroupBuy(Long id) {
        groupBuyRepository.deleteById(id);
    }

    public GroupBuy adminUpdateGroupBuy(Long id, Map<String, Object> data) {
        GroupBuy gb = groupBuyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("团购不存在"));
        buildFromMap(gb, data);
        if (data.containsKey("status")) {
            gb.setStatus(data.get("status").toString());
        }
        return groupBuyRepository.save(gb);
    }

    // ---- 商家端订单 ----

    public List<GroupBuyOrder> getMerchantOrders(Long merchantId) {
        return groupBuyOrderRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    // ---- 私有工具 ----

    private GroupBuy buildFromMap(GroupBuy gb, Map<String, Object> data) {
        if (data.containsKey("name")) gb.setName(data.get("name").toString());
        if (data.containsKey("image")) gb.setImage(data.get("image") != null ? data.get("image").toString() : null);
        if (data.containsKey("groupPrice")) gb.setGroupPrice(new BigDecimal(data.get("groupPrice").toString()));
        if (data.containsKey("originalPrice")) gb.setOriginalPrice(new BigDecimal(data.get("originalPrice").toString()));
        if (data.containsKey("stock")) gb.setStock(Integer.parseInt(data.get("stock").toString()));
        if (data.containsKey("usageDesc")) gb.setUsageDesc(data.get("usageDesc") != null ? data.get("usageDesc").toString() : null);
        if (data.containsKey("detail")) gb.setDetail(data.get("detail") != null ? data.get("detail").toString() : null);
        if (data.containsKey("validStart") && data.get("validStart") != null)
            gb.setValidStart(LocalDate.parse(data.get("validStart").toString()));
        if (data.containsKey("validEnd") && data.get("validEnd") != null)
            gb.setValidEnd(LocalDate.parse(data.get("validEnd").toString()));
        return gb;
    }
}
