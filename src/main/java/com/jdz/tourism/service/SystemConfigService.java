package com.jdz.tourism.service;

import com.jdz.tourism.entity.SystemConfig;
import com.jdz.tourism.repository.SystemConfigRepository;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.repository.ScenicSpotRepository;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.TravelRouteRepository;
import com.jdz.tourism.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SystemConfigService {
    
    @Autowired
    private SystemConfigRepository systemConfigRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ScenicSpotRepository scenicSpotRepository;
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    @Autowired
    private TravelRouteRepository travelRouteRepository;
    
    @Autowired
    private FoodRepository foodRepository;
    
    /**
     * 获取所有系统配置
     */
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }
    
    /**
     * 根据配置键获取配置值
     */
    public String getConfigValue(String configKey) {
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(configKey);
        return config.map(SystemConfig::getConfigValue).orElse(null);
    }
    
    /**
     * 根据配置键获取配置对象
     */
    public Optional<SystemConfig> getConfigByKey(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey);
    }
    
    /**
     * 保存或更新配置
     */
    @Transactional
    public SystemConfig saveOrUpdateConfig(String configKey, String configValue, String description) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKey(configKey);
        
        if (existingConfig.isPresent()) {
            // 更新现有配置
            SystemConfig config = existingConfig.get();
            config.setConfigValue(configValue);
            if (description != null) {
                config.setDescription(description);
            }
            return systemConfigRepository.save(config);
        } else {
            // 创建新配置
            SystemConfig config = new SystemConfig(configKey, configValue, description);
            return systemConfigRepository.save(config);
        }
    }
    
    /**
     * 批量保存系统设置
     */
    @Transactional
    public Map<String, SystemConfig> saveSystemSettings(Map<String, String> settings) {
        Map<String, SystemConfig> result = new HashMap<>();
        
        // 保存系统名称
        if (settings.containsKey("systemName")) {
            result.put("systemName", saveOrUpdateConfig(
                "system.name",
                settings.get("systemName"),
                "系统名称"
            ));
        }
        
        // 保存系统公告
        if (settings.containsKey("announcement")) {
            result.put("announcement", saveOrUpdateConfig(
                "system.announcement",
                settings.get("announcement"),
                "系统公告"
            ));
        }
        
        // 保存推荐算法
        if (settings.containsKey("recommendAlgorithm")) {
            result.put("recommendAlgorithm", saveOrUpdateConfig(
                "system.recommendAlgorithm",
                settings.get("recommendAlgorithm"),
                "推荐算法"
            ));
        }
        
        return result;
    }
    
    /**
     * 获取系统设置（以Map形式返回）
     */
    public Map<String, String> getSystemSettings() {
        Map<String, String> settings = new HashMap<>();
        
        String systemName = getConfigValue("system.name");
        String announcement = getConfigValue("system.announcement");
        String recommendAlgorithm = getConfigValue("system.recommendAlgorithm");
        
        settings.put("systemName", systemName != null ? systemName : "景德镇旅游智能推荐系统");
        settings.put("announcement", announcement != null ? announcement : "");
        settings.put("recommendAlgorithm", recommendAlgorithm != null ? recommendAlgorithm : "hybrid");
        
        return settings;
    }
    
    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(String configKey) {
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(configKey);
        config.ifPresent(systemConfigRepository::delete);
    }
    
    /**
     * 数据备份 - 导出所有数据为JSON格式
     */
    public Map<String, Object> backupData() {
        Map<String, Object> backupData = new HashMap<>();
        
        // 备份时间戳
        backupData.put("backupTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        backupData.put("version", "1.0");
        
        // 备份各表数据
        backupData.put("users", userRepository.findAll());
        backupData.put("scenicSpots", scenicSpotRepository.findAll());
        backupData.put("merchants", merchantRepository.findAll());
        backupData.put("travelRoutes", travelRouteRepository.findAll());
        backupData.put("foods", foodRepository.findAll());
        backupData.put("systemConfigs", systemConfigRepository.findAll());
        
        return backupData;
    }
    
    /**
     * 数据恢复 - 从备份数据恢复
     */
    @Transactional
    public void restoreData(Map<String, Object> backupData) {
        // 注意：恢复数据会清空现有数据，请谨慎操作
        // 这里只恢复非关键数据，用户数据保留
        
        // 恢复景点数据
        if (backupData.containsKey("scenicSpots")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> scenicSpots = (List<Map<String, Object>>) backupData.get("scenicSpots");
            if (scenicSpots != null) {
                // 清空现有景点（可选，根据需求决定）
                // scenicSpotRepository.deleteAll();
                // 恢复景点数据需要转换为实体对象，这里简化处理
            }
        }
        
        // 恢复系统配置
        if (backupData.containsKey("systemConfigs")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> configs = (List<Map<String, Object>>) backupData.get("systemConfigs");
            if (configs != null) {
                for (Map<String, Object> configMap : configs) {
                    String configKey = (String) configMap.get("configKey");
                    String configValue = (String) configMap.get("configValue");
                    String description = (String) configMap.get("description");
                    if (configKey != null && configValue != null) {
                        saveOrUpdateConfig(configKey, configValue, description);
                    }
                }
            }
        }
    }
}

