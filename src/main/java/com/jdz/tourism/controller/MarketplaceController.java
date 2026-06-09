package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Marketplace;
import com.jdz.tourism.service.MarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marketplace")
@CrossOrigin(origins = "*")
@Tag(name = "市集管理", description = "市集相关的API接口")
public class MarketplaceController {

    @Autowired
    private MarketplaceService marketplaceService;

    @Operation(summary = "获取所有市集（管理员）", description = "获取所有市集列表，包括未启用的")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMarketplaces() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Marketplace> list = marketplaceService.getAllMarketplacesForAdmin();
            response.put("success", true);
            response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "获取启用的市集（游客）", description = "获取仅启用的市集列表")
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicMarketplaces() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Marketplace> list = marketplaceService.getEnabledMarketplaces();
            response.put("success", true);
            response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "获取市集详情（游客）", description = "根据ID获取市集详情")
    @GetMapping("/public/{id}")
    public ResponseEntity<Map<String, Object>> getMarketplaceDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Marketplace marketplace = marketplaceService.getById(id)
                    .orElseThrow(() -> new RuntimeException("市集不存在"));
            
            // 检查是否启用
            if (Boolean.FALSE.equals(marketplace.getEnabled())) {
                throw new RuntimeException("该市集未开放");
            }
            
            response.put("success", true);
            response.put("data", marketplace);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "创建市集", description = "创建一个新的市集")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMarketplace(@RequestBody Marketplace marketplace) {
        Map<String, Object> response = new HashMap<>();
        try {
            Marketplace created = marketplaceService.create(marketplace);
            response.put("success", true);
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "更新市集", description = "更新现有的市集信息")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMarketplace(@PathVariable Long id, @RequestBody Marketplace marketplace) {
        Map<String, Object> response = new HashMap<>();
        try {
            Marketplace updated = marketplaceService.update(id, marketplace);
            response.put("success", true);
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "删除市集", description = "根据ID删除市集")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMarketplace(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            marketplaceService.delete(id);
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
