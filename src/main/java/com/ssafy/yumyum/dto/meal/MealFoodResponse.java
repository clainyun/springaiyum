package com.ssafy.yumyum.dto.meal;

import com.ssafy.yumyum.model.FoodItem;

public record MealFoodResponse(
        String code,
        String name,
        String category,
        double grams,
        double energy,
        double carbs,
        double protein,
        double fat
) {
    public static MealFoodResponse from(FoodItem food) {
        return new MealFoodResponse(
                food.getCode(),
                food.getName(),
                food.getCategory(),
                food.getGrams(),
                food.getEnergy(),
                food.getCarbs(),
                food.getProtein(),
                food.getFat()
        );
    }
}
