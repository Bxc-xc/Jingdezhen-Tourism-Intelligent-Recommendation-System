package com.jdz.tourism.service;

import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.MerchantFavorite;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.MerchantFavoriteRepository;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MerchantFavoriteService {

    @Autowired
    private MerchantFavoriteRepository merchantFavoriteRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RealtimeDataService realtimeDataService;

    public MerchantFavorite addFavorite(Long userId, Long merchantId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new RuntimeException("商家不存在"));
        if (merchantFavoriteRepository.existsByUserIdAndMerchantId(userId, merchantId)) {
            throw new RuntimeException("已经收藏过该商家");
        }
        MerchantFavorite fav = new MerchantFavorite(user, merchant);
        MerchantFavorite saved = merchantFavoriteRepository.save(fav);
        realtimeDataService.pushFavoriteUpdate(Map.of(
                "id", saved.getId(),
                "userId", saved.getUser().getId(),
                "merchantId", saved.getMerchant().getId(),
                "createTime", saved.getCreateTime().toString()
        ), "create");
        return saved;
    }

    public void removeFavorite(Long userId, Long merchantId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }
        if (!merchantRepository.existsById(merchantId)) {
            throw new RuntimeException("商家不存在");
        }
        if (!merchantFavoriteRepository.existsByUserIdAndMerchantId(userId, merchantId)) {
            throw new RuntimeException("收藏记录不存在");
        }
        merchantFavoriteRepository.deleteByUserIdAndMerchantId(userId, merchantId);
        realtimeDataService.pushFavoriteUpdate(Map.of("userId", userId, "merchantId", merchantId), "delete");
    }

    public boolean isFavorited(Long userId, Long merchantId) {
        return merchantFavoriteRepository.existsByUserIdAndMerchantId(userId, merchantId);
    }

    public List<MerchantFavorite> getUserFavorites(Long userId) {
        return merchantFavoriteRepository.findByUserId(userId);
    }
}
