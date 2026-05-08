package com.ssafy.yumyum.model;

public class FoodRecommendation {

    private FoodItem food;
    private int energyGap;

    public FoodItem getFood() {
        return food;
    }

    public void setFood(FoodItem food) {
        this.food = food;
    }

    public int getEnergyGap() {
        return energyGap;
    }

    public void setEnergyGap(int energyGap) {
        this.energyGap = energyGap;
    }
}
