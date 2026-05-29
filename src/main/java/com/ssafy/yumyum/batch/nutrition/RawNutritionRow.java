package com.ssafy.yumyum.batch.nutrition;

import java.util.EnumMap;
import java.util.Map;

public record RawNutritionRow(
        String sourceName,
        String sourcePath,
        int sourceRowNo,
        EnumMap<TargetField, String> values
) {
    public String value(TargetField field) {
        return values.get(field);
    }

    public static RawNutritionRow of(String sourceName, String sourcePath, int sourceRowNo,
                                     Map<TargetField, String> values) {
        EnumMap<TargetField, String> copy = new EnumMap<>(TargetField.class);
        copy.putAll(values);
        return new RawNutritionRow(sourceName, sourcePath, sourceRowNo, copy);
    }
}
