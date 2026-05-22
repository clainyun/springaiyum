package com.ssafy.yumyum.dto.meal;

import java.time.LocalDate;

import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.NutritionSummary;

import io.swagger.v3.oas.annotations.media.Schema;

public record MealSummaryResponse(
        @Schema(description = "식단 ID", example = "meal_demo_lunch")
        String id,
        @Schema(description = "식단 날짜", example = "2026-05-22")
        LocalDate mealDate,
        @Schema(description = "식사 유형", example = "lunch")
        String mealType,
        @Schema(description = "메모", example = "채소와 단백질 중심 점심")
        String memo,
        @Schema(description = "식단 영양 요약")
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
