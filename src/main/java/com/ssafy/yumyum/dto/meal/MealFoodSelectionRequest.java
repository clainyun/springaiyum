package com.ssafy.yumyum.dto.meal;

import io.swagger.v3.oas.annotations.media.Schema;

public record MealFoodSelectionRequest(
        @Schema(description = "선택한 식품 코드", example = "food_oat")
        String code,
        @Schema(description = "선택한 중량(g)", example = "180.0")
        Double grams
) {
}
