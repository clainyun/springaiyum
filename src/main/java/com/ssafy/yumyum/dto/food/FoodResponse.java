package com.ssafy.yumyum.dto.food;

import com.ssafy.yumyum.model.FoodItem;

import io.swagger.v3.oas.annotations.media.Schema;

public record FoodResponse(
        @Schema(description = "식품 코드", example = "food_oat")
        String code,
        @Schema(description = "식품 이름", example = "오트밀")
        String name,
        @Schema(description = "식품 분류", example = "곡류")
        String category,
        @Schema(description = "기준 중량(g)", example = "100.0")
        double grams,
        @Schema(description = "열량(kcal)", example = "380.0")
        double energy,
        @Schema(description = "탄수화물(g)", example = "66.0")
        double carbs,
        @Schema(description = "단백질(g)", example = "13.0")
        double protein,
        @Schema(description = "지방(g)", example = "7.0")
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
