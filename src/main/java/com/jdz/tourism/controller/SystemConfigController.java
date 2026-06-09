package com.jdz.tourism.controller;

import com.jdz.tourism.entity.SystemConfig;
import com.jdz.tourism.service.SystemConfigService;
import com.jdz.tourism.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-config")
@CrossOrigin(origins = "*")
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取所有系统配置（需要管理员权限）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllConfigs(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证管理员权限
            if (!validateAdmin(authorization, response)) {
                return ResponseEntity.status(401).body(response);
            }
            
            List<SystemConfig> configs = systemConfigService.getAllConfigs();
            response.put("success", true);
            response.put("data", configs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取系统设置（公开接口，用于前端显示）
     */
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSystemSettings() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, String> settings = systemConfigService.getSystemSettings();
            response.put("success", true);
            response.put("data", settings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据配置键获取配置值（公开接口）
     */
    @GetMapping("/{configKey}")
    public ResponseEntity<Map<String, Object>> getConfigByKey(@PathVariable String configKey) {
        Map<String, Object> response = new HashMap<>();
        try {
            String configValue = systemConfigService.getConfigValue(configKey);
            response.put("success", true);
            response.put("data", configValue);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 保存或更新系统设置（需要管理员权限）
     */
    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> saveSystemSettings(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, String> settings) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证管理员权限
            if (!validateAdmin(authorization, response)) {
                return ResponseEntity.status(401).body(response);
            }
            
            Map<String, SystemConfig> savedConfigs = systemConfigService.saveSystemSettings(settings);
            response.put("success", true);
            response.put("message", "设置保存成功");
            response.put("data", savedConfigs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 保存或更新单个配置（需要管理员权限）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, String> configData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证管理员权限
            if (!validateAdmin(authorization, response)) {
                return ResponseEntity.status(401).body(response);
            }
            
            String configKey = configData.get("configKey");
            String configValue = configData.get("configValue");
            String description = configData.get("description");
            
            if (configKey == null || configValue == null) {
                response.put("success", false);
                response.put("message", "配置键和配置值不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            SystemConfig config = systemConfigService.saveOrUpdateConfig(configKey, configValue, description);
            response.put("success", true);
            response.put("message", "配置保存成功");
            response.put("data", config);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除配置（需要管理员权限）
     */
    @DeleteMapping("/{configKey}")
    public ResponseEntity<Map<String, Object>> deleteConfig(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String configKey) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证管理员权限
            if (!validateAdmin(authorization, response)) {
                return ResponseEntity.status(401).body(response);
            }
            
            systemConfigService.deleteConfig(configKey);
            response.put("success", true);
            response.put("message", "配置删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 数据备份（需要管理员权限）
     */
    @PostMapping("/backup")
    public ResponseEntity<Map<String, Object>> backupData(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证管理员权限
            if (!validateAdmin(authorization, response)) {
                return ResponseEntity.status(401).body(response);
            }
            
            Map<String, Object> backupData = systemConfigService.backupData();
            response.put("success", true);
            response.put("message", "数据备份成功");
            response.put("data", backupData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "数据备份失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 数据恢复（需要管理员权限）
     */
    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreData(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> backupData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 验证管理员权限
            if (!validateAdmin(authorization, response)) {
                return ResponseEntity.status(401).body(response);
            }
            
            systemConfigService.restoreData(backupData);
            response.put("success", true);
            response.put("message", "数据恢复成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "数据恢复失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 验证管理员权限
     */
    private boolean validateAdmin(String authorization, Map<String, Object> response) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "未提供认证信息");
            return false;
        }
        
        try {
            String token = authorization.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.put("success", false);
                response.put("message", "Token无效或已过期");
                return false;
            }
            
            // 这里可以添加角色验证，检查是否为管理员
            // 暂时允许所有已登录用户访问
            return true;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "认证失败: " + e.getMessage());
            return false;
        }
    }
}

