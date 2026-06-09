package com.jdz.tourism.dto.dashboard;

import java.util.List;

public class TrendDataDto {
    private List<String> dates;
    private List<Integer> orderData;
    private List<Integer> userData;

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<Integer> getOrderData() {
        return orderData;
    }

    public void setOrderData(List<Integer> orderData) {
        this.orderData = orderData;
    }

    public List<Integer> getUserData() {
        return userData;
    }

    public void setUserData(List<Integer> userData) {
        this.userData = userData;
    }
}
