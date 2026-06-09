package com.jdz.tourism.service;

import com.jdz.tourism.entity.Favorite;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.repository.FavoriteRepository;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.repository.ScenicSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
@Transactional
public class FavoriteService {
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ScenicSpotRepository scenicSpotRepository;
    
    @Autowired
    private RealtimeDataService realtimeDataService;
    
    /**
     * 添加收藏
     */
    @Transactional
    public Favorite addFavorite(Long userId, Long scenicId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证景点是否存在
        ScenicSpot scenicSpot = scenicSpotRepository.findById(scenicId)
                .orElseThrow(() -> new RuntimeException("景点不存在"));
        
        // 检查是否已经收藏
        if (favoriteRepository.existsByUserIdAndScenicSpotId(userId, scenicId)) {
            throw new RuntimeException("已经收藏过该景点");
        }
        
        // 创建收藏
        Favorite favorite = new Favorite(user, scenicSpot);
        Favorite savedFavorite = favoriteRepository.save(favorite);
        
        // 推送实时更新 - 发送简化的数据避免序列化问题
        Map<String, Object> favoriteData = Map.of(
            "id", savedFavorite.getId(),
            "userId", savedFavorite.getUser().getId(),
            "scenicId", savedFavorite.getScenicSpot().getId(),
            "createTime", savedFavorite.getCreateTime().toString()
        );
        realtimeDataService.pushFavoriteUpdate(favoriteData, "create");
        
        return savedFavorite;
    }
    
    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, Long scenicId) {
        // 验证用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证景点是否存在
        if (!scenicSpotRepository.existsById(scenicId)) {
            throw new RuntimeException("景点不存在");
        }
        
        // 如果不存在，直接抛出异常
        if (!favoriteRepository.existsByUserIdAndScenicSpotId(userId, scenicId)) {
            throw new RuntimeException("收藏记录不存在");
        }

        // 使用自定义删除方法（带事务）
        favoriteRepository.deleteByUserIdAndScenicSpotId(userId, scenicId);

        // 推送实时更新
        realtimeDataService.pushFavoriteUpdate(Map.of("userId", userId, "scenicId", scenicId), "delete");
    }

    /**
     * 根据景点ID删除所有收藏记录
     */
    @Transactional
    public void deleteFavoritesByScenicSpotId(Long scenicSpotId) {
        favoriteRepository.deleteByScenicSpotId(scenicSpotId);
    }
    
    /**
     * 获取用户收藏列表
     */
    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }
    
    /**
     * 检查用户是否收藏了某个景点
     */
    public boolean isFavorited(Long userId, Long scenicId) {
        return favoriteRepository.existsByUserIdAndScenicSpotId(userId, scenicId);
    }
    
    /**
     * 获取用户收藏的景点列表
     */
    public List<ScenicSpot> getUserFavoriteScenics(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(Favorite::getScenicSpot)
                .toList();
    }
}