package com.ssafy.yumyum.batch.nutrition;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.batch.item.ItemProcessor;

public class NutritionNormalizeProcessor implements ItemProcessor<NutritionStagingRow, NutritionProcessResult> {

    @Override
    public NutritionProcessResult process(NutritionStagingRow row) {
        try {
            String foodName = required(row.rawFoodName(), "food_name");
            BigDecimal energyKcal = requiredDecimal(row.rawEnergyKcal(), "energy_kcal");
            String foodCode = foodCode(row.rawFoodCode(), row.sourceName(), foodName, row.rawCategory(), row.rawWeight());
            FoodNutritionUpsert food = new FoodNutritionUpsert(
                    foodCode,
                    limit(foodName, 200),
                    limit(trimToNull(row.rawCategory()), 100),
                    limit(trimToNull(row.rawWeight()), 50),
                    energyKcal,
                    parseDecimal(row.rawProteinG()),
                    parseDecimal(row.rawFatG()),
                    parseDecimal(row.rawCarbohydrateG()),
                    parseDecimal(row.rawSugarG()),
                    parseDecimal(row.rawSodiumMg()),
                    parseDecimal(row.rawCholesterolMg()),
                    parseDecimal(row.rawSaturatedFatG()),
                    parseDecimal(row.rawTransFatG()),
                    parseDecimal(row.rawCaffeineMg())
            );
            return NutritionProcessResult.success(row.stagingId(), food);
        } catch (IllegalArgumentException e) {
            return NutritionProcessResult.failure(row.stagingId(), e.getMessage());
        }
    }

    public static String foodCode(String rawFoodCode, String sourceName, String foodName, String category, String weight) {
        String cleaned = trimToNull(rawFoodCode);
        if (cleaned != null) {
            if (cleaned.length() > 50) {
                throw new IllegalArgumentException("food_code is longer than 50 characters");
            }
            return cleaned;
        }
        return stableFoodCode(sourceName, foodName, category, weight);
    }

    public static String stableFoodCode(String sourceName, String foodName, String category, String weight) {
        String seed = value(sourceName) + "|" + value(foodName) + "|" + value(category) + "|" + value(weight);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String hash = HexFormat.of().formatHex(digest.digest(seed.getBytes(StandardCharsets.UTF_8)));
            return "SRC_" + hash.substring(0, 46);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    public static BigDecimal parseDecimal(String value) {
        String cleaned = trimToNull(value);
        if (cleaned == null || "-".equals(cleaned) || "N/A".equalsIgnoreCase(cleaned)) {
            return null;
        }
        cleaned = cleaned.replace(",", "")
                .replace("<", "")
                .replace(">", "")
                .replace("약", "")
                .replaceAll("[^0-9.\\-]", "");
        if (cleaned.isBlank() || ".".equals(cleaned) || "-".equals(cleaned)) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal requiredDecimal(String value, String fieldName) {
        BigDecimal decimal = parseDecimal(value);
        if (decimal == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return decimal;
    }

    private static String required(String value, String fieldName) {
        String cleaned = trimToNull(value);
        if (cleaned == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return cleaned;
    }

    private static String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private static String value(String value) {
        return trimToNull(value) == null ? "" : trimToNull(value);
    }
}
