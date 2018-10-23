package org.services.test.entity.dto;

import java.io.Serializable;

public class TripId implements Serializable{

    private static final long serialVersionUID = -1351773949188982928L;
    private Type type;
    private String number;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
