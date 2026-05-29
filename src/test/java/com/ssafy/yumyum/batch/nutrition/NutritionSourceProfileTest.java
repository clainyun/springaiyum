package com.ssafy.yumyum.batch.nutrition;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class NutritionSourceProfileTest {

    @Test
    void mapsKoreanHeadersToTargetFields() {
        NutritionSourceProfile profile = NutritionSourceProfile.resolve("test.csv", "test.csv");

        RawNutritionRow row = profile.mapRow("test.csv", "test.csv", 2, Map.of(
                "식품코드", "A001",
                "식품명", "김치찌개",
                "식품대분류명", "찌개",
                "에너지(kcal)", "120",
                "단백질(g)", "7.5"
        ));

        assertThat(row.value(TargetField.FOOD_CODE)).isEqualTo("A001");
        assertThat(row.value(TargetField.FOOD_NAME)).isEqualTo("김치찌개");
        assertThat(row.value(TargetField.CATEGORY)).isEqualTo("찌개");
        assertThat(row.value(TargetField.ENERGY_KCAL)).isEqualTo("120");
        assertThat(row.value(TargetField.PROTEIN_G)).isEqualTo("7.5");
    }

    @Test
    void normalizesHeaderSpacingAndCase() {
        NutritionSourceProfile profile = NutritionSourceProfile.resolve("test.csv", "test.csv");

        assertThat(profile.targetField(" 에너지 (kcal) ")).isEqualTo(TargetField.ENERGY_KCAL);
        assertThat(profile.targetField("FOOD_NAME")).isEqualTo(TargetField.FOOD_NAME);
    }
}
