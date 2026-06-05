package com.ssafy.yumyum.dto.meal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.MealAnalysis;
import com.ssafy.yumyum.model.NutritionSummary;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

public record MealDetailResponse(
        @Schema(description = "식단 ID", example = "meal_demo_lunch")
        String id,
        @Schema(description = "사용자 ID", example = "user_demo")
        String userId,
        @Schema(description = "식단 날짜", example = "2026-05-22")
        LocalDate mealDate,
        @Schema(description = "식사 유형", example = "lunch")
        String mealType,
        @Schema(description = "메모", example = "채소와 단백질 중심 점심")
        String memo,
        @ArraySchema(schema = @Schema(implementation = MealFoodResponse.class))
        List<MealFoodResponse> foods,
        @Schema(description = "식단 영양 요약")
        MealNutritionResponse nutrition,
        @Schema(description = "식단 분석 결과")
        MealAnalysisResponse analysis,
        @Schema(description = "사용자 일일 목표")
        DailyGoalResponse dailyGoal,
        @Schema(description = "생성 시각")
        LocalDateTime createdAt,
        @Schema(description = "수정 시각")
        LocalDateTime updatedAt
) {
    public static MealDetailResponse from(Meal meal, NutritionSummary summary) {
        return from(meal, summary, null, null);
    }

    public static MealDetailResponse from(Meal meal, NutritionSummary summary, MealAnalysis analysis, DailyGoal dailyGoal) {
        List<MealFoodResponse> foods = meal.getFoods().stream()
                .map(MealFoodResponse::from)
                .toList();

        return new MealDetailResponse(
                meal.getId(),
                meal.getUserId(),
                meal.getMealDate(),
                meal.getMealType(),
                meal.getMemo(),
                foods,
                MealNutritionResponse.from(summary),
                analysis == null ? null : MealAnalysisResponse.from(analysis),
                dailyGoal == null ? null : DailyGoalResponse.from(dailyGoal),
                meal.getCreatedAt(),
                meal.getUpdatedAt()
        );
    }
}
