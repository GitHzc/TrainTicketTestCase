package org.services.test.entity.dto;

import java.io.Serializable;

public class StationNameResponseDto implements Serializable {
    private static final long serialVersionUID = -7572926159755384613L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}