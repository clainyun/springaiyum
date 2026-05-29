package com.ssafy.yumyum.batch.nutrition;

public record NutritionImportFailureSample(
        int sourceRowNo,
        String rawFoodName,
        String errorMessage
) {
}
