package com.ssafy.yumyum.dto.meal;

import java.util.List;

import com.ssafy.yumyum.model.MealAnalysis;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

public record MealAnalysisResponse(
        @Schema(description = "식단 영양 요약")
        MealNutritionResponse nutrition,
        @Schema(description = "분석 헤드라인", example = "대체로 좋은 식단입니다.")
        String headline,
        @Schema(description = "다음 행동 제안")
        String nextAction,
        @Schema(description = "식단 등급", example = "B")
        String grade,
        @Schema(description = "식단 점수", example = "84")
        int score,
        @ArraySchema(schema = @Schema(description = "분석 인사이트"))
        List<String> insights
) {
    public static MealAnalysisResponse from(MealAnalysis analysis) {
        return new MealAnalysisResponse(
                MealNutritionResponse.from(analysis.getNutrition()),
                analysis.getHeadline(),
                analysis.getNextAction(),
                analysis.getGrade(),
                analysis.getScore(),
                analysis.getInsights()
        );
    }
}
