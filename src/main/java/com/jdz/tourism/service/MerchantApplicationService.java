package com.jdz.tourism.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.MerchantApplication;
import com.jdz.tourism.repository.MerchantApplicationRepository;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.websocket.DataSyncWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class MerchantApplicationService {
    
    @Autowired
    private MerchantApplicationRepository merchantApplicationRepository;
    
    @Autowired(required = false)
    private MerchantRepository merchantRepository;

    @Autowired(required = false)
    private DataSyncWebSocketHandler webSocketHandler;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 提交商家资质申请
     */
    public MerchantApplication submitApplication(Long userId, String businessLicense, 
                                               String identityCard, String shopPhotos, 
                                               String description) {
        // 检查用户是否已有非拒绝状态的申请（待审核或已通过）
        boolean hasNonRejectedApplication = 
            merchantApplicationRepository.existsNonRejectedApplicationByUserId(userId);
        
        if (hasNonRejectedApplication) {
            // 获取用户最新的申请以确定具体状态
            java.util.List<MerchantApplication> userApplications = 
                merchantApplicationRepository.findByUserIdOrderByCreatedAtDesc(userId);
            
            if (!userApplications.isEmpty()) {
                MerchantApplication latestApplication = userApplications.get(0);
                MerchantApplication.ApplicationStatus latestStatus = latestApplication.getStatus();
                
                if (latestStatus == MerchantApplication.ApplicationStatus.PENDING) {
                    throw new RuntimeException("您已有一个待审核的申请，请等待审核结果");
                } else if (latestStatus == MerchantApplication.ApplicationStatus.APPROVED) {
                    throw new RuntimeException("您的商家资质已通过审核，无需重复申请");
                }
            }
        }
        
        MerchantApplication application = new MerchantApplication(
            userId, businessLicense, identityCard, shopPhotos, description
        );
        
        return merchantApplicationRepository.save(application);
    }
    
    /**
     * 根据用户ID获取申请（取最新一条，避免多条记录时抛出异常）
     */
    @Transactional(readOnly = true)
    public Optional<MerchantApplication> getApplicationByUserId(Long userId) {
        List<MerchantApplication> applications = merchantApplicationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return applications.isEmpty() ? Optional.empty() : Optional.of(applications.get(0));
    }
    
    /**
     * 根据ID获取申请
     */
    @Transactional(readOnly = true)
    public Optional<MerchantApplication> getApplicationById(Long id) {
        return merchantApplicationRepository.findById(id);
    }
    
    /**
     * 获取所有申请
     */
    @Transactional(readOnly = true)
    public List<MerchantApplication> getAllApplications(String status) {
        if (status == null || status.isEmpty()) {
            return merchantApplicationRepository.findAll();
        }
        
        try {
            MerchantApplication.ApplicationStatus applicationStatus = 
                MerchantApplication.ApplicationStatus.valueOf(status.toUpperCase());
            return merchantApplicationRepository.findByStatus(applicationStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的状态参数: " + status);
        }
    }
    
    /**
     * 审核申请
     */
    public MerchantApplication auditApplication(Long id, String status, String opinion) {
        Optional<MerchantApplication> applicationOpt = merchantApplicationRepository.findById(id);
        
        if (!applicationOpt.isPresent()) {
            throw new RuntimeException("申请不存在");
        }
        
        MerchantApplication application = applicationOpt.get();
        
        if (application.getStatus() != MerchantApplication.ApplicationStatus.PENDING) {
            throw new RuntimeException("该申请已被审核，无法重复操作");
        }
        
        try {
            MerchantApplication.ApplicationStatus newStatus = 
                MerchantApplication.ApplicationStatus.valueOf(status.toUpperCase());
            application.setStatus(newStatus);
            application.setAuditOpinion(opinion);
            application.setUpdatedAt(LocalDateTime.now());
            
            MerchantApplication savedApplication = merchantApplicationRepository.save(application);
            
            // 审核通过时，将对应商家的 enabled 设为 true，使其在游客端可见
            if (newStatus == MerchantApplication.ApplicationStatus.APPROVED && merchantRepository != null) {
                merchantRepository.findByUserIdAndDeletedFalse(application.getUserId()).ifPresent(merchant -> {
                    merchant.setEnabled(true);
                    merchantRepository.save(merchant);
                });
            }
            
            // 发送WebSocket通知给商家
            sendAuditNotification(savedApplication);
            
            return savedApplication;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的状态参数: " + status);
        }
    }
    
    /**
     * 获取申请统计信息
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getApplicationStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        stats.put("pending", merchantApplicationRepository.countPendingApplications());
        stats.put("approved", merchantApplicationRepository.countApprovedApplications());
        stats.put("rejected", merchantApplicationRepository.countRejectedApplications());
        
        return stats;
    }
    
    /**
     * 根据条件查找申请
     */
    @Transactional(readOnly = true)
    public List<MerchantApplication> findApplicationsWithFilters(String status, 
                                                                LocalDateTime startDate, 
                                                                LocalDateTime endDate) {
        MerchantApplication.ApplicationStatus applicationStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                applicationStatus = MerchantApplication.ApplicationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的状态参数: " + status);
            }
        }
        
        return merchantApplicationRepository.findApplicationsWithFilters(applicationStatus, startDate, endDate);
    }
    
    /**
     * 发送审核通知（通过WebSocket）
     */
    private void sendAuditNotification(MerchantApplication application) {
        if (webSocketHandler != null) {
            try {
                // 发送数据更新通知
                Map<String, Object> applicationData = new HashMap<>();
                applicationData.put("id", application.getId());
                applicationData.put("userId", application.getUserId());
                applicationData.put("status", application.getStatus().name());
                applicationData.put("auditOpinion", application.getAuditOpinion());
                applicationData.put("updatedAt", application.getUpdatedAt());
                
                webSocketHandler.broadcastDataUpdate("merchant_application", applicationData, "update");
                
                // 发送通知消息给特定用户
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("type", "merchant_application_audited");
                notificationData.put("userId", application.getUserId());
                notificationData.put("status", application.getStatus().name().toLowerCase());
                notificationData.put("auditOpinion", application.getAuditOpinion());
                notificationData.put("message", application.getStatus() == MerchantApplication.ApplicationStatus.APPROVED 
                    ? "您的商家资质申请已通过审核" 
                    : "您的商家资质申请未通过审核");
                
                // 创建通知消息
                String notificationMessage = createNotificationMessage(notificationData);
                webSocketHandler.broadcastMessage(notificationMessage);
            } catch (Exception e) {
                // WebSocket通知失败不影响主流程，只记录日志
                System.err.println("发送WebSocket通知失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 创建通知消息
     */
    private String createNotificationMessage(Map<String, Object> data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "notification");
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            System.err.println("创建通知消息失败: " + e.getMessage());
            return "{\"type\":\"notification\",\"data\":{},\"timestamp\":" + System.currentTimeMillis() + "}";
        }
    }
}