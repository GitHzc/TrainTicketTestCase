package org.services.test.entity.dto;

import java.io.Serializable;

public class Food implements Serializable {
    private static final long serialVersionUID = -111133846889544099L;

    private String foodName;
    private double price;

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
