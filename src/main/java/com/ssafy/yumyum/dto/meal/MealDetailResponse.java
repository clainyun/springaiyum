package com.ssafy.yumyum.dto.meal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.NutritionSummary;

public record MealDetailResponse(
        String id,
        String userId,
        LocalDate mealDate,
        String mealType,
        String memo,
        List<MealFoodResponse> foods,
        MealNutritionResponse nutrition,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MealDetailResponse from(Meal meal, NutritionSummary summary) {
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
                meal.getCreatedAt(),
                meal.getUpdatedAt()
        );
    }
}
