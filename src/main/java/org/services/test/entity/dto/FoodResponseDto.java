package org.services.test.entity.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class FoodResponseDto extends BasicMessage implements Serializable {
    private static final long serialVersionUID = 2095263371638513466L;

    private List<TrainFood> trainFoodList;

    private Map<String, List<FoodStore>> foodStoreListMap;

    public List<TrainFood> getTrainFoodList() {
        return trainFoodList;
    }

    public void setTrainFoodList(List<TrainFood> trainFoodList) {
        this.trainFoodList = trainFoodList;
    }

    public Map<String, List<FoodStore>> getFoodStoreListMap() {
        return foodStoreListMap;
    }

    public void setFoodStoreListMap(Map<String, List<FoodStore>> foodStoreListMap) {
        this.foodStoreListMap = foodStoreListMap;
    }
}
