package com.jdz.tourism.dto.dashboard;

public class StatCardsDto {
    private long totalUsers;
    private long totalOrders;
    private int todayNewUsers;
    private int todayOrders;
    private double userTrend;
    private double orderTrend;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getTodayNewUsers() {
        return todayNewUsers;
    }

    public void setTodayNewUsers(int todayNewUsers) {
        this.todayNewUsers = todayNewUsers;
    }

    public int getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(int todayOrders) {
        this.todayOrders = todayOrders;
    }

    public double getUserTrend() {
        return userTrend;
    }

    public void setUserTrend(double userTrend) {
        this.userTrend = userTrend;
    }

    public double getOrderTrend() {
        return orderTrend;
    }

    public void setOrderTrend(double orderTrend) {
        this.orderTrend = orderTrend;
    }
}
