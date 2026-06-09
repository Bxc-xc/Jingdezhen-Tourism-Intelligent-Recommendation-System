package com.jdz.tourism.service;

import com.jdz.tourism.websocket.DataSyncWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class RealtimeDataService {

    @Autowired
    private DataSyncWebSocketHandler webSocketHandler;

    /**
     * 推送用户数据更新
     */
    public void pushUserUpdate(Object user, String operation) {
        webSocketHandler.broadcastDataUpdate("user", user, operation);
    }

    /**
     * 推送景点数据更新
     */
    public void pushScenicSpotUpdate(Object scenicSpot, String operation) {
        webSocketHandler.broadcastDataUpdate("scenic_spot", scenicSpot, operation);
    }

    /**
     * 推送商家数据更新
     */
    public void pushMerchantUpdate(Object merchant, String operation) {
        webSocketHandler.broadcastDataUpdate("merchant", merchant, operation);
    }

    /**
     * 推送订单数据更新
     */
    public void pushOrderUpdate(Object order, String operation) {
        webSocketHandler.broadcastDataUpdate("order", order, operation);
    }

    public void pushReservationUpdate(Object reservation, String operation) {
        webSocketHandler.broadcastDataUpdate("reservation", reservation, operation);
    }

    /**
     * 推送评论数据更新
     */
    public void pushReviewUpdate(Object review, String operation) {
        webSocketHandler.broadcastDataUpdate("review", review, operation);
    }

    /**
     * 推送收藏数据更新
     */
    public void pushFavoriteUpdate(Object favorite, String operation) {
        webSocketHandler.broadcastDataUpdate("favorite", favorite, operation);
    }

    /**
     * 推送行程规划数据更新
     */
    public void pushTravelPlanUpdate(Object travelPlan, String operation) {
        webSocketHandler.broadcastDataUpdate("travel_plan", travelPlan, operation);
    }

    /**
     * 推送系统通知
     */
    public void pushNotification(String title, String content, String type) {
        webSocketHandler.broadcastDataUpdate("notification", Map.of(
            "title", title,
            "content", content,
            "type", type
        ), "create");
    }
}
