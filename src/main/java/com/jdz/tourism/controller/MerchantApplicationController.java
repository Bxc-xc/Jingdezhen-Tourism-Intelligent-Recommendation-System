package com.jdz.tourism.controller;

import com.jdz.tourism.entity.MerchantApplication;
import com.jdz.tourism.service.MerchantApplicationService;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/merchant-application")
@CrossOrigin(origins = "*")
public class MerchantApplicationController {
    
    @Autowired
    private MerchantApplicationService merchantApplicationService;
    
    /**
     * 提交商家资质申请
     */
    @LogOperation("提交商家申请")
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitApplication(@RequestBody Map<String, Object> applicationData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(applicationData.get("userId").toString());
            String businessLicense = processField(applicationData.get("businessLicense"));
            String identityCard = processField(applicationData.get("identityCard"));
            String shopPhotos = processField(applicationData.get("shopPhotos"));
            String description = applicationData.get("description").toString();
            
            MerchantApplication application = merchantApplicationService.submitApplication(
                userId, businessLicense, identityCard, shopPhotos, description
            );
            
            response.put("success", true);
            response.put("message", "申请提交成功");
            response.put("data", application);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private String processField(Object fieldData) {
        if (fieldData == null) {
            return "";
        }
        if (fieldData instanceof List) {
            List<?> list = (List<?>) fieldData;
            return list.stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining(","));
        }
        return fieldData.toString();
    }
    
    /**
     * 获取用户的申请状态
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getApplicationByUserId(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<MerchantApplication> application = merchantApplicationService.getApplicationByUserId(userId);
            if (application.isPresent()) {
                response.put("success", true);
                response.put("data", application.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", true);
                response.put("data", null);
                response.put("message", "暂无申请记录");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取所有申请（管理员用）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllApplications(
            @RequestParam(required = false) String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<MerchantApplication> applications = merchantApplicationService.getAllApplications(status);
            response.put("success", true);
            response.put("data", applications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 审核申请
     */
    @PutMapping("/{id}/audit")
    public ResponseEntity<Map<String, Object>> auditApplication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> auditData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String status = auditData.get("status").toString();
            String opinion = auditData.get("opinion").toString();
            
            MerchantApplication application = merchantApplicationService.auditApplication(id, status, opinion);
            
            response.put("success", true);
            response.put("message", "审核完成");
            response.put("data", application);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取申请详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getApplicationById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<MerchantApplication> application = merchantApplicationService.getApplicationById(id);
            if (application.isPresent()) {
                response.put("success", true);
                response.put("data", application.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "申请不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}