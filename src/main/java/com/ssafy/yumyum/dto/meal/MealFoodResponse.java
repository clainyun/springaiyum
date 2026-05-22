package com.ssafy.yumyum.dto.meal;

import com.ssafy.yumyum.model.FoodItem;

import io.swagger.v3.oas.annotations.media.Schema;

public record MealFoodResponse(
        @Schema(description = "식품 코드", example = "food_oat")
        String code,
        @Schema(description = "식품 이름", example = "오트밀")
        String name,
        @Schema(description = "식품 분류", example = "곡류")
        String category,
        @Schema(description = "선택한 중량(g)", example = "180.0")
        double grams,
        @Schema(description = "열량(kcal)", example = "684.0")
        double energy,
        @Schema(description = "탄수화물(g)", example = "118.8")
        double carbs,
        @Schema(description = "단백질(g)", example = "23.4")
        double protein,
        @Schema(description = "지방(g)", example = "12.6")
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
