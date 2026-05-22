package com.ssafy.yumyum.dto.meal;

import com.ssafy.yumyum.model.NutritionSummary;

public record MealNutritionResponse(
        double calories,
        double carbs,
        double protein,
        double fat,
        int carbsPct,
        int proteinPct,
        int fatPct
) {
    public static MealNutritionResponse from(NutritionSummary summary) {
        return new MealNutritionResponse(
                summary.getCalories(),
                summary.getCarbs(),
                summary.getProtein(),
                summary.getFat(),
                summary.getCarbsPct(),
                summary.getProteinPct(),
                summary.getFatPct()
        );
    }
}
