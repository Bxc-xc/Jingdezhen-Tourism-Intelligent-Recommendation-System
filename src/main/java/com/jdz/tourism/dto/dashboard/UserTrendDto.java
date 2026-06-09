package com.jdz.tourism.dto.dashboard;

import java.util.List;

public class UserTrendDto {
    private List<String> dates;
    private List<Integer> userData;

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<Integer> getUserData() {
        return userData;
    }

    public void setUserData(List<Integer> userData) {
        this.userData = userData;
    }
}
