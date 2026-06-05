package com.ssafy.yumyum.dto.meal;

import com.ssafy.yumyum.model.DailyGoal;

import io.swagger.v3.oas.annotations.media.Schema;

public record DailyGoalResponse(
        @Schema(description = "일일 목표 열량(kcal)", example = "2200")
        int calories,
        @Schema(description = "일일 목표 탄수화물(g)", example = "275")
        int carbs,
        @Schema(description = "일일 목표 단백질(g)", example = "138")
        int protein,
        @Schema(description = "일일 목표 지방(g)", example = "61")
        int fat
) {
    public static DailyGoalResponse from(DailyGoal goal) {
        return new DailyGoalResponse(
                goal.getCalories(),
                goal.getCarbs(),
                goal.getProtein(),
                goal.getFat()
        );
    }
}
