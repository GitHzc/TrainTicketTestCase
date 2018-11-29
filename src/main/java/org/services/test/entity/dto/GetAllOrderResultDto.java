package org.services.test.entity.dto;

import java.util.ArrayList;

public class GetAllOrderResultDto {

    private boolean status;

    private String message;

    private ArrayList<Order> orders;

    public GetAllOrderResultDto(){

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

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }
}
