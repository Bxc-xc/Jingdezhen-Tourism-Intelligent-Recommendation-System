package com.jdz.tourism.service;

import com.jdz.tourism.entity.Activity;
import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.repository.ActivityRepository;
import com.jdz.tourism.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityService {
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    public List<Activity> getActivitiesByMerchantId(Long merchantId) {
        return activityRepository.findByMerchantId(merchantId);
    }
    
    public List<Activity> getActiveActivitiesByMerchantId(Long merchantId) {
        return activityRepository.findActiveActivitiesByMerchant(merchantId, LocalDateTime.now());
    }
    
    public List<Activity> getAllActiveActivities() {
        return activityRepository.findAllActiveActivities(LocalDateTime.now());
    }
    
    public Optional<Activity> getActivityById(Long id) {
        return activityRepository.findById(id);
    }
    
    @Transactional
    public Activity createActivity(Long merchantId, Activity activity) {
        Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
        if (merchantOpt.isEmpty()) {
            throw new IllegalArgumentException("商家不存在");
        }
        activity.setMerchant(merchantOpt.get());
        return activityRepository.save(activity);
    }
    
    @Transactional
    public Activity updateActivity(Long id, Activity activityData) {
        Optional<Activity> activityOpt = activityRepository.findById(id);
        if (activityOpt.isEmpty()) {
            throw new IllegalArgumentException("活动不存在");
        }
        Activity activity = activityOpt.get();
        if (activityData.getTitle() != null) {
            activity.setTitle(activityData.getTitle());
        }
        if (activityData.getDescription() != null) {
            activity.setDescription(activityData.getDescription());
        }
        if (activityData.getStartTime() != null) {
            activity.setStartTime(activityData.getStartTime());
        }
        if (activityData.getEndTime() != null) {
            activity.setEndTime(activityData.getEndTime());
        }
        if (activityData.getImages() != null) {
            activity.setImages(activityData.getImages());
        }
        if (activityData.getIsActive() != null) {
            activity.setIsActive(activityData.getIsActive());
        }
        return activityRepository.save(activity);
    }
    
    @Transactional
    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }
}

