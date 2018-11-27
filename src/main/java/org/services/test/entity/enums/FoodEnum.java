package org.services.test.entity.enums;

public enum FoodEnum {
    SOUP("Soup", 1, 3.7), NOODLES("Spicy hot noodles", 1, 5), CURD("Oily bean curd", 1, 2);
    private String name;
    private int type;
    private double price;

    FoodEnum(String name, int type, double price) {
        this.name = name;
        this.type = type;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }
}
