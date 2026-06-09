package com.jdz.tourism.repository;

import com.jdz.tourism.entity.GroupBuy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {

    List<GroupBuy> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    List<GroupBuy> findByMerchantIdAndStatusOrderByCreatedAtDesc(Long merchantId, String status);

    @Query("SELECT g FROM GroupBuy g JOIN FETCH g.merchant WHERE g.merchant.id = :merchantId AND g.status = :status ORDER BY g.createdAt DESC")
    List<GroupBuy> findApprovedWithMerchant(@Param("merchantId") Long merchantId, @Param("status") String status);

    List<GroupBuy> findByStatusOrderByCreatedAtDesc(String status);

    List<GroupBuy> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE GroupBuy g SET g.soldCount = g.soldCount + :qty, g.stock = g.stock - :qty WHERE g.id = :id AND g.stock >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);
}
