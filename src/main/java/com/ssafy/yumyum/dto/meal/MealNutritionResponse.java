package com.ssafy.yumyum.dto.meal;

import com.ssafy.yumyum.model.NutritionSummary;

import io.swagger.v3.oas.annotations.media.Schema;

public record MealNutritionResponse(
        @Schema(description = "총 열량(kcal)", example = "512.4")
        double calories,
        @Schema(description = "총 탄수화물(g)", example = "53.0")
        double carbs,
        @Schema(description = "총 단백질(g)", example = "30.4")
        double protein,
        @Schema(description = "총 지방(g)", example = "14.2")
        double fat,
        @Schema(description = "탄수화물 비율(%)", example = "41")
        int carbsPct,
        @Schema(description = "단백질 비율(%)", example = "24")
        int proteinPct,
        @Schema(description = "지방 비율(%)", example = "25")
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
