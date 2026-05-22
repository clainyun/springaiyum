package com.ssafy.yumyum.dto.food;

import com.ssafy.yumyum.model.FoodItem;

public record FoodResponse(
        String code,
        String name,
        String category,
        double grams,
        double energy,
        double carbs,
        double protein,
        double fat
) {
    public static FoodResponse from(FoodItem food) {
        return new FoodResponse(
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
