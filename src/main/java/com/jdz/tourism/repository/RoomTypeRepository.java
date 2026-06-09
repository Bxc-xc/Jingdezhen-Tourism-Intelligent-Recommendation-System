package com.jdz.tourism.repository;

import com.jdz.tourism.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findByMerchantId(Long merchantId);

    /** 管理员用：查所有房型，按商家ID排序 */
    @Query("SELECT r FROM RoomType r JOIN FETCH r.merchant ORDER BY r.merchant.id, r.id")
    List<RoomType> findAllWithMerchant();

    /** 按商家ID删除所有房型（删商家时级联用） */
    void deleteByMerchantId(Long merchantId);
}
