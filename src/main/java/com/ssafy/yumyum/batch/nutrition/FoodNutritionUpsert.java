package com.ssafy.yumyum.batch.nutrition;

import java.math.BigDecimal;

public record FoodNutritionUpsert(
        String foodCode,
        String foodName,
        String category,
        String weight,
        BigDecimal energyKcal,
        BigDecimal proteinG,
        BigDecimal fatG,
        BigDecimal carbohydrateG,
        BigDecimal sugarG,
        BigDecimal sodiumMg,
        BigDecimal cholesterolMg,
        BigDecimal saturatedFatG,
        BigDecimal transFatG,
        BigDecimal caffeineMg
) {
}
