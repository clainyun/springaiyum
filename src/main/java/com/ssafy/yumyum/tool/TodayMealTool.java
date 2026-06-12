package com.ssafy.yumyum.tool;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.NutritionSummary;
import com.ssafy.yumyum.service.MealService;

@Component
public class TodayMealTool {

    private static final Logger log = LoggerFactory.getLogger(TodayMealTool.class);

    private final MealService mealService;

    public TodayMealTool(MealService mealService) {
        this.mealService = mealService;
    }

    @Tool(description = "사용자의 오늘 날짜 식단 목록과 총 영양 섭취량(칼로리, 탄수화물, 단백질, 지방)을 조회합니다.")
    public TodayMealSummary getTodayMeals(
            @ToolParam(description = "조회할 사용자의 ID") String userId) {

        log.info("[Tool 호출] TodayMealTool.getTodayMeals - userId: {}", userId);

        LocalDate today = LocalDate.now();
        List<Meal> todayMeals = mealService.getMealsForUser(userId).stream()
                .filter(meal -> today.equals(meal.getMealDate()))
                .toList();

        List<FoodItem> allFoods = todayMeals.stream()
                .flatMap(meal -> meal.getFoods().stream())
                .toList();

        NutritionSummary nutrition = mealService.summarize(allFoods);

        List<String> mealDescriptions = todayMeals.stream()
                .map(meal -> meal.getMealType() + ": " + meal.getFoods().stream()
                        .map(FoodItem::getName)
                        .collect(Collectors.joining(", ")))
                .toList();

        TodayMealSummary result = new TodayMealSummary(
                today.toString(),
                todayMeals.size(),
                nutrition.getCalories(),
                nutrition.getCarbs(),
                nutrition.getProtein(),
                nutrition.getFat(),
                nutrition.getCarbsPct(),
                nutrition.getProteinPct(),
                nutrition.getFatPct(),
                mealDescriptions
        );

        log.info("[Tool 결과] TodayMealTool - 오늘 {}끼 기록, 총 {}kcal (탄{}% 단{}% 지{}%)",
                todayMeals.size(),
                nutrition.getCalories(),
                nutrition.getCarbsPct(),
                nutrition.getProteinPct(),
                nutrition.getFatPct());

        return result;
    }

    public record TodayMealSummary(
            String date,
            int mealCount,
            double totalCalories,
            double totalCarbs,
            double totalProtein,
            double totalFat,
            int carbsPercent,
            int proteinPercent,
            int fatPercent,
            List<String> mealDescriptions
    ) {}
}
