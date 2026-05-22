package com.ssafy.yumyum.dto.meal;

import java.util.List;

public record MealRequest(
        String mealDate,
        String mealType,
        String memo,
        List<MealFoodSelectionRequest> foods
) {
}
