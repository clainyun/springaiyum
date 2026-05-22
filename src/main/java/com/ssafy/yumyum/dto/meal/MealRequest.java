package com.ssafy.yumyum.dto.meal;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

public record MealRequest(
        @Schema(description = "식단 날짜", example = "2026-05-22")
        String mealDate,
        @Schema(description = "식사 유형", example = "lunch")
        String mealType,
        @Schema(description = "메모", example = "채소와 단백질 중심 점심")
        String memo,
        @ArraySchema(schema = @Schema(implementation = MealFoodSelectionRequest.class))
        List<MealFoodSelectionRequest> foods
) {
}
