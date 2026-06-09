package com.jdz.tourism.service;

import com.jdz.tourism.entity.Marketplace;
import com.jdz.tourism.entity.MarketplaceFavorite;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.MarketplaceFavoriteRepository;
import com.jdz.tourism.repository.MarketplaceRepository;
import com.jdz.tourism.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class MarketplaceFavoriteService {

    @Autowired
    private MarketplaceFavoriteRepository marketplaceFavoriteRepository;
    @Autowired
    private MarketplaceRepository marketplaceRepository;
    @Autowired
    private UserRepository userRepository;

    public MarketplaceFavorite addFavorite(Long userId, Long marketplaceId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Marketplace marketplace = marketplaceRepository.findById(marketplaceId).orElseThrow(() -> new RuntimeException("市集不存在"));
        if (marketplaceFavoriteRepository.existsByUserIdAndMarketplaceId(userId, marketplaceId)) {
            throw new RuntimeException("已经收藏过该市集");
        }
        return marketplaceFavoriteRepository.save(new MarketplaceFavorite(user, marketplace));
    }

    public void removeFavorite(Long userId, Long marketplaceId) {
        if (!marketplaceFavoriteRepository.existsByUserIdAndMarketplaceId(userId, marketplaceId)) {
            throw new RuntimeException("收藏记录不存在");
        }
        marketplaceFavoriteRepository.deleteByUserIdAndMarketplaceId(userId, marketplaceId);
    }

    public boolean isFavorited(Long userId, Long marketplaceId) {
        return marketplaceFavoriteRepository.existsByUserIdAndMarketplaceId(userId, marketplaceId);
    }

    public List<MarketplaceFavorite> getUserFavorites(Long userId) {
        return marketplaceFavoriteRepository.findByUserId(userId);
    }
}
