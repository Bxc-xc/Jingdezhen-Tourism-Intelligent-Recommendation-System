package com.jdz.tourism.service;

import com.jdz.tourism.entity.TravelPlan;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.repository.TravelPlanRepository;
import com.jdz.tourism.repository.TravelPlanVersionRepository;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.repository.ScenicSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import com.jdz.tourism.entity.TravelPlanVersion;
import com.jdz.tourism.entity.TravelRoute;
import com.jdz.tourism.repository.TravelRouteRepository;

@Service
public class TravelPlanService {
    
    @Autowired
    private TravelPlanRepository travelPlanRepository;
    
    @Autowired
    private TravelPlanVersionRepository travelPlanVersionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ScenicSpotRepository scenicSpotRepository;
    
    @Autowired
    private TravelRouteRepository travelRouteRepository;
    
    @Autowired
    private RealtimeDataService realtimeDataService;
    
    /**
     * 从Map中安全获取字符串值，支持多个字段名映射
     */
    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }
    
    /**
     * 从Map中安全获取对象值，支持多个字段名映射
     */
    @SuppressWarnings("unchecked")
    private <T> T getValue(Map<String, Object> map, Class<T> clazz, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return (T) value;
            }
        }
        return null;
    }
    
    /**
     * 生成行程规划
     */
    public TravelPlan generatePlan(Long userId, Map<String, Object> planRequest) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取请求参数，支持新旧字段名映射
        // 安全获取 days
        Object daysObj = planRequest.get("days");
        if (daysObj == null) {
            throw new RuntimeException("days 参数不能为空");
        }
        Integer days = Integer.valueOf(daysObj.toString());
        
        // 安全获取 startDate
        String startDateStr = getStringValue(planRequest, "startDate");
        if (startDateStr == null || startDateStr.trim().isEmpty()) {
            throw new RuntimeException("startDate 参数不能为空");
        }
        // 支持 budget 和 budgetType 两种字段名
        String budget = getStringValue(planRequest, "budget", "budgetType");
        if (budget == null) {
            budget = "medium"; // 默认值
        }
        // 支持 transport 和 transportType 两种字段名
        String transport = getStringValue(planRequest, "transport", "transportType");
        if (transport == null) {
            transport = "walking"; // 默认值
        }
        // 支持 interests 字段（可选）
        @SuppressWarnings("unchecked")
        List<String> interests = (List<String>) getValue(planRequest, List.class, "interests");
        if (interests == null) {
            interests = new ArrayList<>();
        }
        
        // 解析开始日期
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = startDate.plusDays(days - 1);
        
        // 生成行程标题（如果前端未提供，则自动生成）
        String title = getStringValue(planRequest, "title");
        if (title == null || title.trim().isEmpty()) {
            title = generatePlanTitle(days, interests);
        }
        
        // 生成行程描述（如果前端未提供，则自动生成）
        String description = getStringValue(planRequest, "description");
        if (description == null || description.trim().isEmpty()) {
            description = generatePlanDescription(days, budget, transport, interests);
        }
        
        // 计算总费用
        BigDecimal totalCost = calculateTotalCost(days, budget, transport);
        
        // 生成行程详情（如果前端未提供，则自动生成）
        String planDetails = getStringValue(planRequest, "planDetails");
        if (planDetails == null || planDetails.trim().isEmpty()) {
            planDetails = generatePlanDetails(days, startDate, interests);
        }
        
        // 创建行程对象
        TravelPlan plan = new TravelPlan();
        plan.setUser(user);
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setDays(days);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan.setTotalCost(totalCost);
        plan.setBudgetType(budget);
        plan.setTransportType(transport);
        plan.setInterests(String.join(",", interests));
        plan.setPlanDetails(planDetails);
        plan.setCreateTime(LocalDateTime.now());
        
        // 保存行程
        TravelPlan savedPlan = travelPlanRepository.save(plan);
        
        // 推送实时更新
        realtimeDataService.pushTravelPlanUpdate(savedPlan, "create");
        
        return savedPlan;
    }
    
    /**
     * 获取用户行程列表
     */
    public List<TravelPlan> getUserPlans(Long userId) {
        return travelPlanRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
    
    /**
     * 根据ID获取行程详情
     */
    public Optional<TravelPlan> getPlanById(Long planId) {
        return travelPlanRepository.findById(planId);
    }
    
    /**
     * 更新行程
     */
    public TravelPlan updatePlan(Long planId, TravelPlan planData) {
        TravelPlan existingPlan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("行程不存在"));

        // 先保存旧版本快照，支持版本回滚
        savePlanVersionSnapshot(existingPlan);
        
        // 更新字段
        if (planData.getTitle() != null) existingPlan.setTitle(planData.getTitle());
        if (planData.getDescription() != null) existingPlan.setDescription(planData.getDescription());
        if (planData.getPlanDetails() != null) existingPlan.setPlanDetails(planData.getPlanDetails());
        existingPlan.setUpdateTime(LocalDateTime.now());
        
        TravelPlan updatedPlan = travelPlanRepository.save(existingPlan);
        
        // 推送实时更新
        realtimeDataService.pushTravelPlanUpdate(updatedPlan, "update");
        
        return updatedPlan;
    }
    
    /**
     * 获取指定行程的版本列表（按版本号倒序）
     */
    public List<TravelPlanVersion> getPlanVersions(Long planId) {
        return travelPlanVersionRepository.findByPlanIdOrderByVersionNumberDesc(planId);
    }
    
    /**
     * 从指定版本恢复行程
     */
    public TravelPlan restoreFromVersion(Long planId, Long versionId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("行程不存在"));
        
        TravelPlanVersion version = travelPlanVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("版本不存在"));
        
        if (!version.getPlan().getId().equals(planId)) {
            throw new RuntimeException("版本与行程不匹配");
        }
        
        // 将版本内容覆盖回当前行程，但仍然会产生一个新的版本快照
        savePlanVersionSnapshot(plan);
        
        plan.setTitle(version.getTitle());
        plan.setDescription(version.getDescription());
        plan.setPlanDetails(version.getPlanDetails());
        plan.setUpdateTime(LocalDateTime.now());
        
        TravelPlan updatedPlan = travelPlanRepository.save(plan);
        realtimeDataService.pushTravelPlanUpdate(updatedPlan, "update");
        return updatedPlan;
    }
    
    /**
     * 删除行程
     */
    public void deletePlan(Long planId) {
        if (!travelPlanRepository.existsById(planId)) {
            throw new RuntimeException("行程不存在");
        }
        
        // 先删除所有关联的版本历史记录
        List<TravelPlanVersion> versions = travelPlanVersionRepository.findByPlanIdOrderByVersionNumberDesc(planId);
        if (!versions.isEmpty()) {
            travelPlanVersionRepository.deleteAll(versions);
        }
        
        // 然后删除行程本身
        travelPlanRepository.deleteById(planId);
        
        // 推送实时更新
        realtimeDataService.pushTravelPlanUpdate(Map.of("id", planId), "delete");
    }

    /**
     * 删除指定用户的所有行程（含版本历史），用于删除用户时级联清理
     */
    public void deletePlansByUserId(Long userId) {
        List<TravelPlan> plans = travelPlanRepository.findByUserId(userId);
        for (TravelPlan plan : plans) {
            List<TravelPlanVersion> versions = travelPlanVersionRepository.findByPlanIdOrderByVersionNumberDesc(plan.getId());
            if (!versions.isEmpty()) {
                travelPlanVersionRepository.deleteAll(versions);
            }
        }
        travelPlanRepository.deleteAll(plans);
    }
    
    /**
     * 生成行程标题
     */
    private String generatePlanTitle(Integer days, List<String> interests) {
        String interestStr = String.join("、", interests);
        return String.format("%d天%s之旅", days, interestStr);
    }
    
    /**
     * 生成行程描述
     */
    private String generatePlanDescription(Integer days, String budget, String transport, List<String> interests) {
        return String.format("一次为期%d天的%s旅行，预算%s，交通方式%s，主要兴趣：%s", 
                days, "景德镇", budget, transport, String.join("、", interests));
    }
    
    /**
     * 计算总费用
     */
    private BigDecimal calculateTotalCost(Integer days, String budget, String transport) {
        int baseCost = switch (budget) {
            case "low" -> 200;
            case "medium" -> 400;
            case "high" -> 600;
            default -> 300;
        };
        
        int transportCost = switch (transport) {
            case "car" -> 100;
            case "bus" -> 50;
            case "train" -> 80;
            case "plane" -> 200;
            default -> 60;
        };
        
        return BigDecimal.valueOf((baseCost + transportCost) * days);
    }
    
    /**
     * 生成行程详情
     */
    private String generatePlanDetails(Integer days, LocalDate startDate, List<String> interests) {
        // 获取所有景点
        List<ScenicSpot> allScenics = scenicSpotRepository.findAll();
        
        if (allScenics.isEmpty()) {
            throw new RuntimeException("系统中暂无景点数据，无法生成行程");
        }
        
        Random random = new Random();
        StringBuilder details = new StringBuilder();
        details.append("{\"days\":[");
        
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            details.append("{");
            details.append("\"day\":").append(i + 1).append(",");
            details.append("\"date\":\"").append(currentDate.toString()).append("\",");
            details.append("\"title\":\"第").append(i + 1).append("天行程\",");
            details.append("\"schedule\":[");
            
            // 每天安排2-3个景点
            int scenicCount = 2 + random.nextInt(2);
            // 确保不超过可用景点数量
            scenicCount = Math.min(scenicCount, allScenics.size());
            
            // 随机选择不重复的景点
            List<ScenicSpot> selectedScenics = new ArrayList<>();
            Set<Integer> usedIndices = new HashSet<>();
            
            for (int j = 0; j < scenicCount; j++) {
                int index;
                do {
                    index = random.nextInt(allScenics.size());
                } while (usedIndices.contains(index) && usedIndices.size() < allScenics.size());
                
                usedIndices.add(index);
                selectedScenics.add(allScenics.get(index));
            }
            
            for (int j = 0; j < selectedScenics.size(); j++) {
                ScenicSpot scenic = selectedScenics.get(j);
                int hour = 9 + j * 3;
                String timeStr = String.format("%02d:00", hour);
                
                // 生成时长（1-3小时，根据景点类型调整）
                String duration = generateDuration(scenic);
                
                // 获取门票价格
                String ticketPrice = scenic.getPrice() != null 
                    ? scenic.getPrice().toString() 
                    : "0";
                
                // 生成游玩tips
                String tips = generateTips(scenic);
                
                details.append("{");
                details.append("\"time\":\"").append(timeStr).append("\",");
                details.append("\"title\":\"").append(escapeJson(scenic.getName())).append("\",");
                details.append("\"description\":\"").append(escapeJson(
                    scenic.getDescription() != null && !scenic.getDescription().isEmpty() 
                        ? scenic.getDescription().substring(0, Math.min(50, scenic.getDescription().length()))
                        : "游览" + scenic.getName()
                )).append("\",");
                details.append("\"duration\":\"").append(escapeJson(duration)).append("\",");
                details.append("\"ticketPrice\":").append(ticketPrice).append(",");
                details.append("\"tips\":\"").append(escapeJson(tips)).append("\",");
                details.append("\"tags\":[");
                
                // 添加标签
                if (scenic.getCategory() != null) {
                    details.append("\"").append(escapeJson(scenic.getCategory())).append("\"");
                }
                if (scenic.getTags() != null && !scenic.getTags().isEmpty()) {
                    String[] tags = scenic.getTags().split(",");
                    for (int k = 0; k < Math.min(2, tags.length); k++) {
                        if (k > 0 || scenic.getCategory() != null) details.append(",");
                        details.append("\"").append(escapeJson(tags[k].trim())).append("\"");
                    }
                }
                
                details.append("]");
                details.append("}");
                if (j < selectedScenics.size() - 1) details.append(",");
            }
            
            details.append("]");
            details.append("}");
            if (i < days - 1) details.append(",");
        }
        
        details.append("]}");
        return details.toString();
    }
    
    /**
     * 从推荐路线生成行程（用于行程模板功能）
     */
    public TravelPlan createPlanFromRoute(Long userId, Long routeId, LocalDate startDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        TravelRoute route = travelRouteRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("路线不存在"));
        
        int days = route.getDays() != null ? route.getDays() : 1;
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        LocalDate endDate = startDate.plusDays(days - 1);
        
        // 基于路线信息构造基础行程
        String title = route.getName() != null ? route.getName() : "推荐路线行程";
        String description = route.getDescription() != null ? route.getDescription() : "基于推荐路线自动生成的行程";
        
        // scenicSpots 字段为 JSON 字符串，这里做一个简单解析，按顺序平分到每天
        List<Map<String, Object>> scenicList = new ArrayList<>();
        try {
            if (route.getScenicSpots() != null && !route.getScenicSpots().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                scenicList = mapper.readValue(route.getScenicSpots(), List.class);
            }
        } catch (Exception e) {
            // 解析失败则留空，由用户后续自行编辑
        }
        
        StringBuilder details = new StringBuilder();
        details.append("{\"days\":[");
        
        int totalSpots = scenicList.size();
        int index = 0;
        
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            details.append("{");
            details.append("\"day\":").append(i + 1).append(",");
            details.append("\"date\":\"").append(currentDate.toString()).append("\",");
            details.append("\"title\":\"第").append(i + 1).append("天·").append(escapeJson(title)).append("\",");
            details.append("\"schedule\":[");
            
            // 粗略地将景点平均分配到每天
            int remainingDays = days - i;
            int remainingSpots = totalSpots - index;
            int todayCount = remainingDays > 0 ? (int) Math.ceil(remainingSpots / (double) remainingDays) : remainingSpots;
            todayCount = Math.max(todayCount, 0);
            
            for (int j = 0; j < todayCount && index < totalSpots; j++, index++) {
                Map<String, Object> scenic = scenicList.get(index);
                String name = scenic.getOrDefault("name", "景点").toString();
                String duration = scenic.getOrDefault("duration", "2小时").toString();
                String ticketPrice = scenic.getOrDefault("ticketPrice", scenic.getOrDefault("price", "0")).toString();
                String tips = scenic.getOrDefault("tips", "建议提前了解景点信息，合理安排游览时间").toString();
                int hour = 9 + j * 3;
                String timeStr = String.format("%02d:00", hour);
                
                details.append("{");
                details.append("\"time\":\"").append(timeStr).append("\",");
                details.append("\"title\":\"").append(escapeJson(name)).append("\",");
                details.append("\"description\":\"预计游览时长 ").append(escapeJson(duration)).append("\",");
                details.append("\"duration\":\"").append(escapeJson(duration)).append("\",");
                details.append("\"ticketPrice\":").append(ticketPrice).append(",");
                details.append("\"tips\":\"").append(escapeJson(tips)).append("\",");
                details.append("\"tags\":[\"推荐路线\"]");
                details.append("}");
                if (j < todayCount - 1 && index < totalSpots - 1) {
                    details.append(",");
                }
            }
            
            details.append("]");
            details.append("}");
            if (i < days - 1) {
                details.append(",");
            }
        }
        
        details.append("]}");
        
        TravelPlan plan = new TravelPlan();
        plan.setUser(user);
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setDays(days);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan.setPlanDetails(details.toString());
        plan.setCreateTime(LocalDateTime.now());
        
        TravelPlan savedPlan = travelPlanRepository.save(plan);
        realtimeDataService.pushTravelPlanUpdate(savedPlan, "create");
        return savedPlan;
    }
    
    /**
     * 为行程保存一个版本快照
     */
    private void savePlanVersionSnapshot(TravelPlan plan) {
        TravelPlanVersion version = new TravelPlanVersion();
        version.setPlan(plan);
        // 计算下一个版本号
        int nextVersion = travelPlanVersionRepository
                .findTopByPlanIdOrderByVersionNumberDesc(plan.getId())
                .map(TravelPlanVersion::getVersionNumber)
                .orElse(0) + 1;
        version.setVersionNumber(nextVersion);
        version.setTitle(plan.getTitle());
        version.setDescription(plan.getDescription());
        version.setPlanDetails(plan.getPlanDetails());
        travelPlanVersionRepository.save(version);
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 生成景点游玩时长
     */
    private String generateDuration(ScenicSpot scenic) {
        Random random = new Random();
        // 根据景点类型生成合理的时长
        String category = scenic.getCategory();
        if (category != null) {
            if (category.contains("博物馆") || category.contains("文化")) {
                return (1 + random.nextInt(2)) + "小时";
            } else if (category.contains("公园") || category.contains("自然")) {
                return (2 + random.nextInt(2)) + "小时";
            } else if (category.contains("古迹") || category.contains("历史")) {
                return (1 + random.nextInt(2)) + "小时";
            }
        }
        // 默认1-3小时
        return (1 + random.nextInt(3)) + "小时";
    }
    
    /**
     * 生成景点游玩tips
     */
    private String generateTips(ScenicSpot scenic) {
        List<String> tipsList = new ArrayList<>();
        Random random = new Random();
        
        // 基于景点信息生成tips
        if (scenic.getCategory() != null) {
            String category = scenic.getCategory();
            if (category.contains("博物馆")) {
                tipsList.add("建议提前预约，避开周末人流高峰");
                tipsList.add("可关注官方公众号获取免费讲解");
            } else if (category.contains("公园") || category.contains("自然")) {
                tipsList.add("适合拍照，建议早上或傍晚前往");
                tipsList.add("注意防晒，带好驱蚊液");
            } else if (category.contains("古迹") || category.contains("历史")) {
                tipsList.add("了解历史文化背景后游览更有意义");
                tipsList.add("红墙竹影拍照绝美，早到避开人流");
            }
        }
        
        // 通用tips
        if (scenic.getPrice() != null && scenic.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            tipsList.add("门票价格：" + scenic.getPrice() + "元");
        }
        
        if (scenic.getOpenTime() != null && !scenic.getOpenTime().isEmpty()) {
            tipsList.add("开放时间：" + scenic.getOpenTime());
        }
        
        // 如果没有特定tips，添加通用建议
        if (tipsList.isEmpty()) {
            tipsList.add("建议提前了解景点信息，合理安排游览时间");
            tipsList.add("注意景区开放时间，避免错过最佳游览时段");
        }
        
        // 随机选择1-2条tips返回
        int tipCount = Math.min(1 + random.nextInt(2), tipsList.size());
        List<String> selectedTips = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();
        for (int i = 0; i < tipCount && usedIndices.size() < tipsList.size(); i++) {
            int index;
            do {
                index = random.nextInt(tipsList.size());
            } while (usedIndices.contains(index));
            usedIndices.add(index);
            selectedTips.add(tipsList.get(index));
        }
        
        return String.join("；", selectedTips);
    }
}