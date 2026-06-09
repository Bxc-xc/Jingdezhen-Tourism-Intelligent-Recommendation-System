package com.jdz.tourism.repository;

import com.jdz.tourism.entity.MarketplaceFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MarketplaceFavoriteRepository extends JpaRepository<MarketplaceFavorite, Long> {
    boolean existsByUserIdAndMarketplaceId(Long userId, Long marketplaceId);
    void deleteByUserIdAndMarketplaceId(Long userId, Long marketplaceId);
    List<MarketplaceFavorite> findByUserId(Long userId);
}
