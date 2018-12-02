package org.services.test.entity.dto;

import java.util.ArrayList;

public class GetRoutesListlResultDto {

    private boolean status;

    private String message;

    private ArrayList<Route> routes;

    public GetRoutesListlResultDto() {
        //Default Constructor
    }

    public GetRoutesListlResultDto(boolean status, String message, ArrayList<Route> routes) {
        this.status = status;
        this.message = message;
        this.routes = routes;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }
}