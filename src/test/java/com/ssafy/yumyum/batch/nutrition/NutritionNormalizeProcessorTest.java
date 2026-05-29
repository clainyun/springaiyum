package com.ssafy.yumyum.batch.nutrition;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class NutritionNormalizeProcessorTest {

    private final NutritionNormalizeProcessor processor = new NutritionNormalizeProcessor();

    @Test
    void generatedFoodCodeIsStable() {
        String first = NutritionNormalizeProcessor.stableFoodCode("source", "김치찌개", "찌개", "100g");
        String second = NutritionNormalizeProcessor.stableFoodCode("source", "김치찌개", "찌개", "100g");

        assertThat(first).isEqualTo(second);
        assertThat(first).startsWith("SRC_").hasSize(50);
    }

    @Test
    void parsesDecoratedDecimalValues() {
        assertThat(NutritionNormalizeProcessor.parseDecimal("1,234.5 mg")).isEqualByComparingTo(new BigDecimal("1234.5"));
        assertThat(NutritionNormalizeProcessor.parseDecimal("-")).isNull();
    }

    @Test
    void failsWhenFoodNameIsBlank() throws Exception {
        NutritionStagingRow row = row("", "100");

        NutritionProcessResult result = processor.process(row);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("food_name");
    }

    @Test
    void failsWhenEnergyIsBlank() throws Exception {
        NutritionStagingRow row = row("김치찌개", "");

        NutritionProcessResult result = processor.process(row);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("energy_kcal");
    }

    @Test
    void createsUpsertPayload() throws Exception {
        NutritionStagingRow row = row("김치찌개", "120");

        NutritionProcessResult result = processor.process(row);

        assertThat(result.success()).isTrue();
        assertThat(result.food().foodName()).isEqualTo("김치찌개");
        assertThat(result.food().energyKcal()).isEqualByComparingTo("120");
    }

    private NutritionStagingRow row(String foodName, String energyKcal) {
        return new NutritionStagingRow(
                1L, 10L, "source.csv", "source.csv", 2,
                null, foodName, "찌개", "100g", energyKcal,
                "7", "3", "12", "1", "500", "0", "1", "0", null
        );
    }
}
