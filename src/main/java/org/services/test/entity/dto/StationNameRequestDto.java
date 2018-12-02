package org.services.test.entity.dto;

import java.io.Serializable;

public class StationNameRequestDto implements Serializable {
    private static final long serialVersionUID = -529779484475143549L;

    private String stationId;

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }
}
