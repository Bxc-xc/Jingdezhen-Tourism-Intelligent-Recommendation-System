package com.jdz.tourism.repository;

import com.jdz.tourism.entity.Marketplace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketplaceRepository extends JpaRepository<Marketplace, Long> {
    List<Marketplace> findAllByEnabledTrueOrderBySortOrderAscIdAsc();
    List<Marketplace> findAllByOrderBySortOrderAscIdAsc();
}


