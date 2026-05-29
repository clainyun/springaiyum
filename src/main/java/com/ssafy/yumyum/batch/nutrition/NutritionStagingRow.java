package com.ssafy.yumyum.batch.nutrition;

public record NutritionStagingRow(
        long stagingId,
        long jobExecutionId,
        String sourceName,
        String sourcePath,
        int sourceRowNo,
        String rawFoodCode,
        String rawFoodName,
        String rawCategory,
        String rawWeight,
        String rawEnergyKcal,
        String rawProteinG,
        String rawFatG,
        String rawCarbohydrateG,
        String rawSugarG,
        String rawSodiumMg,
        String rawCholesterolMg,
        String rawSaturatedFatG,
        String rawTransFatG,
        String rawCaffeineMg
) {
}
