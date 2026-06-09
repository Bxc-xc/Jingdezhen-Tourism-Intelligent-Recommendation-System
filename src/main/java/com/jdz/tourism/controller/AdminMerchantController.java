package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.service.MerchantService;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 管理员端：餐饮商家管理
 * 功能：新增餐饮商家，并同时创建能登录的商家账号（MERCHANT 角色）
 * 新增即上架（deleted=false），游客端可见（category=FOOD）
 */
@RestController
@RequestMapping("/api/admin/merchant")
@CrossOrigin(origins = "*")
public class AdminMerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 新增餐饮商家（同时创建可登录账号）
     *
     * 请求体示例：
     * {
     *   "username": "food_001",
     *   "password": "123456",
     *   "shopName": "乐味小馆",
     *   "description": "地道赣菜",
     *   "address": "景德镇昌江区陶阳南路88号",
     *   "phone": "0798-1234567",
     *   "openTime": "10:00-22:00",
     *   "startPrice": "45.0",
     *   "avatar": "https://img.example.com/a.jpg",
     *   "images": ["https://img.example.com/a1.jpg","https://img.example.com/a2.jpg"],
     *   "cuisineType": "LOCAL",
     *   "avgPrice": "50-80"
     * }
     */
    @LogOperation("管理员新增餐饮商家")
    @PostMapping("/food")
    public ResponseEntity<Map<String, Object>> createFoodMerchant(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1) 基本参数校验（沿用已有的基础规则：用户名非空唯一、店铺名非空唯一）
            String username = requireNonBlank(body.get("username"), "用户名不能为空");
            String rawPassword = requireNonBlank(body.get("password"), "密码不能为空");
            String shopName = requireNonBlank(body.get("shopName"), "店铺名称不能为空");

            if (userRepository.existsByUsername(username)) {
                throw new RuntimeException("用户名已存在");
            }

            // 2) 创建可登录账号（MERCHANT 角色）
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(User.UserRole.MERCHANT);
            // 可选的手机号/邮箱
            if (body.get("phone") != null) {
                try {
                    // 若不满足实体里正则，JPA校验会抛错；此处不强制
                    user.setPhone(body.get("phone").toString());
                } catch (Exception ignored) {}
            }
            User savedUser = userRepository.save(user);

            // 3) 使用已有Service创建商家（复用 “用户存在/店名唯一/默认deleted=false” 校验）
            Merchant merchant = merchantService.createMerchant(savedUser.getId(), shopName,
                    body.getOrDefault("description", "") != null ? body.get("description").toString() : "", "FOOD");

            // 4) 设置餐饮相关与展示相关字段并保存（可见条件：category=FOOD 且 deleted=false）
            // 地址/电话/营业时间
            if (body.get("address") != null) {
                merchant.setAddress(body.get("address").toString());
            }
            if (body.get("phone") != null) {
                merchant.setPhone(body.get("phone").toString());
            }
            if (body.get("openTime") != null) {
                merchant.setOpenTime(body.get("openTime").toString());
            }
            // 起步价（人均/展示用）
            if (body.get("startPrice") != null && !body.get("startPrice").toString().isBlank()) {
                try {
                    merchant.setStartPrice(new BigDecimal(body.get("startPrice").toString()));
                } catch (NumberFormatException ignored) {}
            }
            // 头像/多图
            if (body.get("avatar") != null) {
                merchant.setAvatar(body.get("avatar").toString());
            }
            if (body.get("images") != null) {
                Object imagesObj = body.get("images");
                if (imagesObj instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) imagesObj;
                    String joined = list.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .filter(s -> !s.isBlank())
                            .distinct()
                            .collect(Collectors.joining(","));
                    merchant.setShopImages(joined.isEmpty() ? null : joined);
                } else {
                    // 兼容直接传字符串的情况
                    merchant.setShopImages(imagesObj.toString());
                }
            }
            // 餐饮扩展
            if (body.get("cuisineType") != null) {
                merchant.setCuisineType(body.get("cuisineType").toString());
            }
            if (body.get("avgPrice") != null) {
                merchant.setAvgPrice(body.get("avgPrice").toString());
            }
            // 确保上架（逻辑删除为 false）
            merchant.setDeleted(false);
            merchant.setDeletedAt(null);

            Merchant saved = merchantService.updateMerchant(merchant);
            Map<String, Object> data = merchantService.enrichMerchantWithRating(saved);

            response.put("success", true);
            response.put("message", "餐饮商家创建成功");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private String requireNonBlank(Object value, String message) {
        if (value == null || value.toString().isBlank()) {
            throw new RuntimeException(message);
        }
        return value.toString();
    }
}

