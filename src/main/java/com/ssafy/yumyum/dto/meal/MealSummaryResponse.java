package com.ssafy.yumyum.dto.meal;

import java.time.LocalDate;

import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.NutritionSummary;

public record MealSummaryResponse(
        String id,
        LocalDate mealDate,
        String mealType,
        String memo,
        MealNutritionResponse nutrition
) {
    public static MealSummaryResponse from(Meal meal, NutritionSummary summary) {
        return new MealSummaryResponse(
                meal.getId(),
                meal.getMealDate(),
                meal.getMealType(),
                meal.getMemo(),
                MealNutritionResponse.from(summary)
        );
    }
}
