package com.jdz.tourism.service;

import com.jdz.tourism.entity.GroupBuy;
import com.jdz.tourism.entity.GroupBuyOrder;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.GroupBuyOrderRepository;
import com.jdz.tourism.repository.GroupBuyRepository;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupBuyServiceTest {

    @Mock private GroupBuyRepository groupBuyRepository;
    @Mock private GroupBuyOrderRepository groupBuyOrderRepository;
    @Mock private MerchantRepository merchantRepository;
    @Mock private UserRepository userRepository;
    @Mock private RealtimeDataService realtimeDataService;

    @InjectMocks
    private GroupBuyService groupBuyService;

    private Merchant testMerchant;
    private User testUser;
    private GroupBuy testGroupBuy;

    @BeforeEach
    void setUp() {
        testMerchant = new Merchant();
        testMerchant.setId(1L);
        testMerchant.setShopName("陶瓷工坊");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("tourist1");

        testGroupBuy = new GroupBuy();
        testGroupBuy.setId(1L);
        testGroupBuy.setMerchant(testMerchant);
        testGroupBuy.setName("陶瓷体验团购");
        testGroupBuy.setGroupPrice(new BigDecimal("99.00"));
        testGroupBuy.setOriginalPrice(new BigDecimal("150.00"));
        testGroupBuy.setStock(50);
        testGroupBuy.setSoldCount(0);
        testGroupBuy.setStatus("APPROVED");
        testGroupBuy.setValidEnd(LocalDate.now().plusDays(30));
    }

    // ---- 商家端：创建团购 ----

    @Test
    void createGroupBuy_ShouldCreateWithPendingStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "新团购");
        data.put("groupPrice", "88.00");
        data.put("originalPrice", "120.00");
        data.put("stock", "100");
        data.put("validEnd", LocalDate.now().plusDays(30).toString());

        when(merchantRepository.findById(1L)).thenReturn(Optional.of(testMerchant));
        when(groupBuyRepository.save(any(GroupBuy.class))).thenAnswer(inv -> inv.getArgument(0));

        GroupBuy result = groupBuyService.createGroupBuy(1L, data);

        assertEquals("PENDING", result.getStatus());
        assertEquals("新团购", result.getName());
        assertEquals(0, result.getSoldCount());
        verify(groupBuyRepository).save(any(GroupBuy.class));
    }

    @Test
    void createGroupBuy_ShouldThrowWhenMerchantNotFound() {
        when(merchantRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupBuyService.createGroupBuy(999L, new HashMap<>()));
        assertEquals("商家不存在", ex.getMessage());
        verify(groupBuyRepository, never()).save(any());
    }

    // ---- 商家端：更新团购 ----

    @Test
    void updateGroupBuy_ShouldResetToPending() {
        testGroupBuy.setStatus("APPROVED");
        Map<String, Object> data = new HashMap<>();
        data.put("name", "修改后的团购");

        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));
        when(groupBuyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GroupBuy result = groupBuyService.updateGroupBuy(1L, 1L, data);

        assertEquals("PENDING", result.getStatus()); // 修改后重新审核
        assertEquals("修改后的团购", result.getName());
    }

    @Test
    void updateGroupBuy_ShouldThrowWhenNotOwner() {
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupBuyService.updateGroupBuy(1L, 999L, new HashMap<>()));
        assertEquals("无权操作", ex.getMessage());
    }

    // ---- 商家端：删除团购 ----

    @Test
    void deleteGroupBuy_ShouldDeleteWhenOwner() {
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));

        groupBuyService.deleteGroupBuy(1L, 1L);

        verify(groupBuyRepository).delete(testGroupBuy);
    }

    @Test
    void deleteGroupBuy_ShouldThrowWhenNotOwner() {
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));

        assertThrows(RuntimeException.class, () -> groupBuyService.deleteGroupBuy(1L, 999L));
        verify(groupBuyRepository, never()).delete(any());
    }

    // ---- 用户端：下单 ----

    @Test
    void placeOrder_ShouldSucceedWithValidData() {
        Map<String, Object> data = new HashMap<>();
        data.put("groupBuyId", "1");
        data.put("quantity", "2");
        data.put("useDate", LocalDate.now().plusDays(5).toString());
        data.put("contactName", "张三");
        data.put("contactPhone", "13800138000");

        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));
        when(groupBuyRepository.decreaseStock(1L, 2)).thenReturn(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(groupBuyOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GroupBuyOrder order = groupBuyService.placeOrder(1L, data);

        assertEquals(2, order.getQuantity());
        assertEquals(new BigDecimal("198.00"), order.getTotalPrice());
        assertEquals("PAID", order.getStatus());
        verify(groupBuyOrderRepository).save(any());
    }

    @Test
    void placeOrder_ShouldThrowWhenNotApproved() {
        testGroupBuy.setStatus("PENDING");
        Map<String, Object> data = new HashMap<>();
        data.put("groupBuyId", "1");
        data.put("quantity", "1");

        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupBuyService.placeOrder(1L, data));
        assertEquals("该团购暂不可购买", ex.getMessage());
    }

    @Test
    void placeOrder_ShouldThrowWhenStockInsufficient() {
        testGroupBuy.setStock(1);
        Map<String, Object> data = new HashMap<>();
        data.put("groupBuyId", "1");
        data.put("quantity", "5");

        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupBuyService.placeOrder(1L, data));
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    @Test
    void placeOrder_ShouldThrowWhenExpired() {
        testGroupBuy.setValidEnd(LocalDate.now().minusDays(1));
        Map<String, Object> data = new HashMap<>();
        data.put("groupBuyId", "1");
        data.put("quantity", "1");

        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupBuyService.placeOrder(1L, data));
        assertEquals("该团购已过期", ex.getMessage());
    }

    // ---- 管理员端：审核 ----

    @Test
    void auditGroupBuy_ShouldApprove() {
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));
        when(groupBuyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GroupBuy result = groupBuyService.auditGroupBuy(1L, "APPROVED", null);

        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void auditGroupBuy_ShouldRejectWithReason() {
        testGroupBuy.setStatus("PENDING");
        when(groupBuyRepository.findById(1L)).thenReturn(Optional.of(testGroupBuy));
        when(groupBuyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GroupBuy result = groupBuyService.auditGroupBuy(1L, "REJECTED", "价格不合规");

        assertEquals("REJECTED", result.getStatus());
        assertEquals("价格不合规", result.getRejectReason());
    }

    // ---- 查询 ----

    @Test
    void getMerchantGroupBuys_ShouldReturnList() {
        when(groupBuyRepository.findByMerchantIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(testGroupBuy));

        List<GroupBuy> result = groupBuyService.getMerchantGroupBuys(1L);

        assertEquals(1, result.size());
        verify(groupBuyRepository).findByMerchantIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getApprovedGroupBuys_ShouldReturnOnlyApproved() {
        when(groupBuyRepository.findApprovedWithMerchant(1L, "APPROVED"))
                .thenReturn(Arrays.asList(testGroupBuy));

        List<GroupBuy> result = groupBuyService.getApprovedGroupBuys(1L);

        assertEquals(1, result.size());
        assertEquals("APPROVED", result.get(0).getStatus());
    }
}
