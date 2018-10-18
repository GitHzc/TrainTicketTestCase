package org.services.test.entity.dto;

import java.io.Serializable;

public class QueryTicketRequestDto implements Serializable{


    private static final long serialVersionUID = -1026143254492151965L;

    private String startingPlace;
    private String endPlace;
    private String departureTime;

    public String getStartingPlace() {
        return startingPlace;
    }

    public void setStartingPlace(String startingPlace) {
        this.startingPlace = startingPlace;
    }

    public String getEndPlace() {
        return endPlace;
    }

    public void setEndPlace(String endPlace) {
        this.endPlace = endPlace;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }
}
