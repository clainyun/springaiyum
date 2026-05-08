package com.ssafy.yumyum.model;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getGrams() {
        return grams;
    }

    public void setGrams(double grams) {
        this.grams = grams;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }
}
