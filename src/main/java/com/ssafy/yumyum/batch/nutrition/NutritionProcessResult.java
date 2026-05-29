package com.ssafy.yumyum.batch.nutrition;

public record NutritionProcessResult(
        long stagingId,
        FoodNutritionUpsert food,
        String errorMessage
) {
    public static NutritionProcessResult success(long stagingId, FoodNutritionUpsert food) {
        return new NutritionProcessResult(stagingId, food, null);
    }

    public static NutritionProcessResult failure(long stagingId, String errorMessage) {
        return new NutritionProcessResult(stagingId, null, errorMessage);
    }

    public boolean success() {
        return food != null;
    }
}
