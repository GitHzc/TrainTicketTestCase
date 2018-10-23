package org.services.test.entity.dto;

import java.io.Serializable;

public class FoodRequestDto implements Serializable {

    private static final long serialVersionUID = 3710515546247116786L;

    private String date;
    private String startStation;
    private String endStation;
    private String tripId;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartStation() {
        return startStation;
    }

    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public void setEndStation(String endStation) {
        this.endStation = endStation;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
