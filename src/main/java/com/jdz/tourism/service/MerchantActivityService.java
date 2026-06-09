package com.jdz.tourism.service;

import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.MerchantActivity;
import com.jdz.tourism.repository.MerchantActivityRepository;
import com.jdz.tourism.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MerchantActivityService {
    
    @Autowired
    private MerchantActivityRepository activityRepository;
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    /**
     * 创建活动
     */
    public MerchantActivity createActivity(Long merchantId, MerchantActivity activity) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("商家不存在"));
        
        activity.setMerchant(merchant);
        // 如果未设置状态，根据时间自动判断
        if (activity.getStatus() == null || activity.getStatus().equals("active")) {
            updateActivityStatus(activity);
        }
        
        return activityRepository.save(activity);
    }
    
    /**
     * 更新活动
     */
    public MerchantActivity updateActivity(Long activityId, MerchantActivity activityData) {
        MerchantActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        // 更新活动信息
        if (activityData.getTitle() != null) {
            activity.setTitle(activityData.getTitle());
        }
        if (activityData.getDescription() != null) {
            activity.setDescription(activityData.getDescription());
        }
        if (activityData.getType() != null) {
            activity.setType(activityData.getType());
        }
        if (activityData.getImage() != null) {
            activity.setImage(activityData.getImage());
        }
        if (activityData.getDiscount() != null) {
            activity.setDiscount(activityData.getDiscount());
        }
        if (activityData.getStartTime() != null) {
            activity.setStartTime(activityData.getStartTime());
        }
        if (activityData.getEndTime() != null) {
            activity.setEndTime(activityData.getEndTime());
        }
        
        // 更新状态
        updateActivityStatus(activity);
        
        return activityRepository.save(activity);
    }
    
    /**
     * 根据ID获取活动
     */
    public Optional<MerchantActivity> getActivityById(Long activityId) {
        return activityRepository.findById(activityId);
    }
    
    /**
     * 删除活动
     */
    public void deleteActivity(Long activityId) {
        activityRepository.deleteById(activityId);
    }
    
    /**
     * 获取商家的所有活动
     */
    public List<MerchantActivity> getActivitiesByMerchantId(Long merchantId) {
        List<MerchantActivity> activities = activityRepository.findByMerchantIdOrderByStartTimeDesc(merchantId);
        // 每次获取时更新状态（也可以用定时任务，这里简单处理）
        activities.forEach(this::updateActivityStatus);
        return activities;
    }
    
    /**
     * 获取商家的进行中活动
     */
    public List<MerchantActivity> getActiveActivitiesByMerchantId(Long merchantId) {
        return getActivitiesByMerchantId(merchantId).stream()
                .filter(a -> "active".equals(a.getStatus()))
                .collect(Collectors.toList());
    }
    
    /**
     * 更新活动状态
     */
    private void updateActivityStatus(MerchantActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        String newStatus;
        
        if (now.isBefore(activity.getStartTime())) {
            newStatus = "pending";
        } else if (now.isAfter(activity.getEndTime())) {
            newStatus = "ended";
        } else {
            newStatus = "active";
        }
        
        if (!newStatus.equals(activity.getStatus())) {
            activity.setStatus(newStatus);
            activityRepository.save(activity);
        }
    }
}
