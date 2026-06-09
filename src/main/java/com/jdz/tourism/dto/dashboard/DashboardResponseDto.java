package com.jdz.tourism.dto.dashboard;

import java.util.List;

public class DashboardResponseDto {
    private StatCardsDto statCards;
    private TrendDataDto orderTrend7d;
    private TrendDataDto orderTrend30d;
    private UserTrendDto userTrend30d;
    private List<MerchantApplicationItemDto> merchantApplications;

    public StatCardsDto getStatCards() {
        return statCards;
    }

    public void setStatCards(StatCardsDto statCards) {
        this.statCards = statCards;
    }

    public TrendDataDto getOrderTrend7d() {
        return orderTrend7d;
    }

    public void setOrderTrend7d(TrendDataDto orderTrend7d) {
        this.orderTrend7d = orderTrend7d;
    }

    public TrendDataDto getOrderTrend30d() {
        return orderTrend30d;
    }

    public void setOrderTrend30d(TrendDataDto orderTrend30d) {
        this.orderTrend30d = orderTrend30d;
    }

    public UserTrendDto getUserTrend30d() {
        return userTrend30d;
    }

    public void setUserTrend30d(UserTrendDto userTrend30d) {
        this.userTrend30d = userTrend30d;
    }

    public List<MerchantApplicationItemDto> getMerchantApplications() {
        return merchantApplications;
    }

    public void setMerchantApplications(List<MerchantApplicationItemDto> merchantApplications) {
        this.merchantApplications = merchantApplications;
    }
}
