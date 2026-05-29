package com.ssafy.yumyum.batch.nutrition;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NutritionSourceProfile {

    private final Map<String, TargetField> aliasMap;

    private NutritionSourceProfile(Map<String, TargetField> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public static NutritionSourceProfile resolve(String sourceName, String sourcePath) {
        Map<TargetField, List<String>> aliases = new EnumMap<>(TargetField.class);
        add(aliases, TargetField.FOOD_CODE, "식품코드", "식품번호", "food_code", "foodcode", "code");
        add(aliases, TargetField.FOOD_NAME, "식품명", "음식명", "음식이름", "제품명", "식품명칭", "food_name", "foodname");
        add(aliases, TargetField.CATEGORY, "식품대분류명", "식품대분류", "대분류", "유형명", "음식구분", "category");
        add(aliases, TargetField.WEIGHT, "영양성분함량기준량", "식품중량", "중량", "1회제공량", "총내용량", "내용량", "weight");
        add(aliases, TargetField.ENERGY_KCAL, "에너지(kcal)", "열량(kcal)", "에너지", "열량", "칼로리", "1인분칼로리(kcal)", "energy_kcal");
        add(aliases, TargetField.PROTEIN_G, "단백질(g)", "단백질", "protein_g");
        add(aliases, TargetField.FAT_G, "지방(g)", "지방", "fat_g");
        add(aliases, TargetField.CARBOHYDRATE_G, "탄수화물(g)", "탄수화물", "carbohydrate_g");
        add(aliases, TargetField.SUGAR_G, "당류(g)", "당류", "sugar_g");
        add(aliases, TargetField.SODIUM_MG, "나트륨(mg)", "나트륨", "sodium_mg");
        add(aliases, TargetField.CHOLESTEROL_MG, "콜레스테롤(mg)", "콜레스테롤", "콜레스트롤(g)", "콜레스트롤", "cholesterol_mg");
        add(aliases, TargetField.SATURATED_FAT_G, "포화지방산(g)", "포화지방(g)", "포화지방산", "포화지방", "saturated_fat_g");
        add(aliases, TargetField.TRANS_FAT_G, "트랜스지방산(g)", "트랜스지방(g)", "트랜스지방산", "트랜스지방", "trans_fat_g");
        add(aliases, TargetField.CAFFEINE_MG, "카페인(mg)", "카페인", "caffeine_mg");

        Map<String, TargetField> aliasMap = new LinkedHashMap<>();
        aliases.forEach((field, names) -> names.forEach(name -> aliasMap.put(normalizeHeader(name), field)));
        return new NutritionSourceProfile(aliasMap);
    }

    public RawNutritionRow mapRow(String sourceName, String sourcePath, int sourceRowNo, Map<String, String> cellsByHeader) {
        EnumMap<TargetField, String> values = new EnumMap<>(TargetField.class);
        for (Map.Entry<String, String> entry : cellsByHeader.entrySet()) {
            TargetField field = aliasMap.get(normalizeHeader(entry.getKey()));
            if (field != null && !hasText(values.get(field))) {
                values.put(field, cleanCell(entry.getValue()));
            }
        }
        return RawNutritionRow.of(sourceName, sourcePath, sourceRowNo, values);
    }

    public TargetField targetField(String header) {
        return aliasMap.get(normalizeHeader(header));
    }

    static String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\uFEFF", "")
                .replaceAll("\\p{Cntrl}", "")
                .replaceAll("\\s+", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private static String cleanCell(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replace("\uFEFF", "").trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void add(Map<TargetField, List<String>> aliases, TargetField field, String... names) {
        List<String> list = aliases.computeIfAbsent(field, key -> new ArrayList<>());
        for (String name : names) {
            list.add(name);
        }
    }
}
