package com.jdz.tourism.repository;

import com.jdz.tourism.entity.GroupBuyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyOrderRepository extends JpaRepository<GroupBuyOrder, Long> {

    List<GroupBuyOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<GroupBuyOrder> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    List<GroupBuyOrder> findByGroupBuyIdOrderByCreatedAtDesc(Long groupBuyId);
}
