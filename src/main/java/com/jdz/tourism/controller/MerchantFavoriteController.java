package com.jdz.tourism.controller;

import com.jdz.tourism.annotation.LogOperation;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.MerchantFavorite;
import com.jdz.tourism.service.MerchantFavoriteService;
import com.jdz.tourism.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/merchant")
@CrossOrigin(origins = "*")
public class MerchantFavoriteController {

    @Autowired
    private MerchantFavoriteService merchantFavoriteService;
    @Autowired
    private MerchantRepository merchantRepository;

    @LogOperation("添加商家收藏")
    @PostMapping("/favorite")
    public ResponseEntity<Map<String, Object>> addFavorite(@RequestBody Map<String, Object> req) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = Long.valueOf(req.get("userId").toString());
            Long merchantId = Long.valueOf(req.get("merchantId").toString());
            MerchantFavorite favorite = merchantFavoriteService.addFavorite(userId, merchantId);
            res.put("success", true);
            res.put("data", favorite.getId());
            res.put("message", "收藏成功");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @LogOperation("取消商家收藏")
    @DeleteMapping("/favorite/{merchantId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(@PathVariable Long merchantId, @RequestParam Long userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            merchantFavoriteService.removeFavorite(userId, merchantId);
            res.put("success", true);
            res.put("message", "取消收藏成功");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/favorites")
    public ResponseEntity<Map<String, Object>> list(@RequestParam Long userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            List<MerchantFavorite> list = merchantFavoriteService.getUserFavorites(userId);
            // 返回商家精简信息（避免循环序列化）
            List<Map<String, Object>> data = list.stream().map(f -> {
                Merchant m = f.getMerchant();
                Map<String, Object> mm = new HashMap<>();
                mm.put("id", m.getId());
                mm.put("shopName", m.getShopName());
                mm.put("category", m.getCategory());
                mm.put("avatar", m.getAvatar());
                mm.put("address", m.getAddress());
                mm.put("rating", m.getRating());
                mm.put("startPrice", m.getStartPrice());
                mm.put("createTime", f.getCreateTime().toString());
                return mm;
            }).collect(Collectors.toList());
            res.put("success", true);
            res.put("data", data);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/favorite/{merchantId}")
    public ResponseEntity<Map<String, Object>> check(@PathVariable Long merchantId, @RequestParam Long userId) {
        Map<String, Object> res = new HashMap<>();
        try {
            boolean favorited = merchantFavoriteService.isFavorited(userId, merchantId);
            res.put("success", true);
            res.put("data", favorited);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}
