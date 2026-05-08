package com.ssafy.yumyum.model;

import lombok.Data;

@Data
public class NutritionSummary {

    private double calories;
    private double carbs;
    private double protein;
    private double fat;
    private int carbsPct;
    private int proteinPct;
    private int fatPct;
}
