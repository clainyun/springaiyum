package com.ssafy.yumyum.model;

import lombok.Data;

@Data
public class FoodRecommendation {

    private FoodItem food;
    private int energyGap;
}
