package com.jdz.tourism.scripts;

import com.jdz.tourism.entity.Merchant;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.repository.MerchantRepository;
import com.jdz.tourism.repository.ScenicSpotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 景点迁移到商家 - 一次性迁移脚本
 * 
 * 使用方法：
 * 1. 确保数据库已执行 migrate-scenic-to-merchant.sql 添加 scenic_id 字段
 * 2. 将此脚本添加到项目中，运行应用时会自动执行
 * 3. 或者通过Spring Boot的CommandLineRunner机制运行
 * 
 * 功能：
 * - 为每个景点创建对应的商家记录
 * - 设置category为SCENIC
 * - 关联scenic_id
 * - 跳过已存在的记录
 * 
 * 注意：此脚本只会执行一次，执行后可以删除或注释掉
 */
@Component
@Order(1) // 确保在其他初始化之前执行
public class MigrateScenicToMerchantScript implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MigrateScenicToMerchantScript.class);
    
    // 迁移标志，防止重复执行
    private static boolean migrationExecuted = false;

    @Autowired(required = false)
    private ScenicSpotRepository scenicSpotRepository;

    @Autowired(required = false)
    private MerchantRepository merchantRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 检查是否已执行过
        if (migrationExecuted) {
            logger.info("景点迁移脚本已执行过，跳过");
            return;
        }

        // 检查依赖是否注入成功
        if (scenicSpotRepository == null || merchantRepository == null) {
            logger.warn("Repository未注入，跳过迁移脚本");
            return;
        }

        logger.info("========================================");
        logger.info("开始执行景点到商家的迁移...");
        logger.info("========================================");

        try {
            // 获取所有景点
            List<ScenicSpot> scenicSpots = scenicSpotRepository.findAll();
            logger.info("找到 {} 个景点", scenicSpots.size());

            if (scenicSpots.isEmpty()) {
                logger.info("没有找到景点数据，跳过迁移");
                migrationExecuted = true;
                return;
            }

            int successCount = 0;
            int skipCount = 0;
            int errorCount = 0;

            for (ScenicSpot scenic : scenicSpots) {
                try {
                    // 检查是否已存在对应的商家记录（通过scenic_id查找）
                    Optional<Merchant> existingMerchant = merchantRepository.findByScenicSpotId(scenic.getId());
                    if (existingMerchant.isPresent() && "SCENIC".equals(existingMerchant.get().getCategory())) {
                        logger.debug("跳过: {} (已存在商家记录)", scenic.getName());
                        skipCount++;
                        continue;
                    }

                    // 创建商家记录
                    Merchant merchant = new Merchant();
                    merchant.setShopName(scenic.getName());
                    merchant.setDescription(scenic.getDescription());
                    merchant.setCategory("SCENIC");
                    merchant.setScenicSpot(scenic);
                    merchant.setOpenTime(scenic.getOpenTime());
                    // user_id 设置为 null，景点不需要用户账号
                    // 如果需要景点也能登录商家端，可以创建一个系统用户并关联

                    merchantRepository.save(merchant);
                    logger.info("✓ 成功创建商家: {} (Merchant ID: {}, Scenic ID: {})", 
                            scenic.getName(), merchant.getId(), scenic.getId());
                    successCount++;

                } catch (Exception e) {
                    logger.error("✗ 处理景点失败: {} - {}", scenic.getName(), e.getMessage(), e);
                    errorCount++;
                }
            }

            logger.info("========================================");
            logger.info("迁移完成！");
            logger.info("成功: {}", successCount);
            logger.info("跳过: {}", skipCount);
            logger.info("失败: {}", errorCount);
            logger.info("========================================");

            migrationExecuted = true;

        } catch (Exception e) {
            logger.error("迁移过程中发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("迁移失败", e);
        }
    }

    /**
     * 手动执行迁移（用于测试或单独调用）
     */
    public void executeMigration() {
        migrationExecuted = false;
        run();
    }
}

