package com.ssafy.yumyum.model;

import lombok.Data;

@Data
public class FoodItem {

    private String code;
    private String name;
    private String category;
    private double grams;
    private double energy;
    private double carbs;
    private double protein;
    private double fat;

    public FoodItem copyWithGrams(double selectedGrams) {
        FoodItem item = new FoodItem();
        item.setCode(code);
        item.setName(name);
        item.setCategory(category);
        item.setGrams(selectedGrams);

        double ratio = selectedGrams / 100.0;
        item.setEnergy(round(energy * ratio));
        item.setCarbs(round(carbs * ratio));
        item.setProtein(round(protein * ratio));
        item.setFat(round(fat * ratio));
        return item;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
