package com.ssafy.yumyum.batch.nutrition;

public record NutritionImportSummary(
        int total,
        int success,
        int failed,
        int ready
) {
}
