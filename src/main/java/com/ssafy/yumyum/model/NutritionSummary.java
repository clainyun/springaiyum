package com.ssafy.yumyum.model;

public class NutritionSummary {

    private double calories;
    private double carbs;
    private double protein;
    private double fat;
    private int carbsPct;
    private int proteinPct;
    private int fatPct;

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
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

    public int getCarbsPct() {
        return carbsPct;
    }

    public void setCarbsPct(int carbsPct) {
        this.carbsPct = carbsPct;
    }

    public int getProteinPct() {
        return proteinPct;
    }

    public void setProteinPct(int proteinPct) {
        this.proteinPct = proteinPct;
    }

    public int getFatPct() {
        return fatPct;
    }

    public void setFatPct(int fatPct) {
        this.fatPct = fatPct;
    }
}
