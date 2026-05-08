package com.ssafy.yumyum.service;

import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.model.FoodRecommendation;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.MealAnalysis;
import com.ssafy.yumyum.model.NutritionSummary;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.FoodCatalogRepository;
import com.ssafy.yumyum.repository.MealRepository;
import com.ssafy.yumyum.util.IdGenerator;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SortUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MealService {

    private final MealRepository mealRepository;
    private final FoodCatalogRepository foodCatalogRepository;

    public MealService(MealRepository mealRepository, FoodCatalogRepository foodCatalogRepository) {
        this.mealRepository = mealRepository;
        this.foodCatalogRepository = foodCatalogRepository;
    }

    public List<Meal> getMealsForUser(String userId, LocalDate startDate, LocalDate endDate, String mealType, String sortKey, User user) {
        List<Meal> meals = new ArrayList<>();
        for (Meal meal : mealRepository.findAll()) {
            if (!userId.equals(meal.getUserId())) {
                continue;
            }
            if (startDate != null && meal.getMealDate().isBefore(startDate)) {
                continue;
            }
            if (endDate != null && meal.getMealDate().isAfter(endDate)) {
                continue;
            }
            if (mealType != null && !mealType.isEmpty() && !mealType.equals(meal.getMealType())) {
                continue;
            }
            meals.add(meal);
        }
        return sortMeals(meals, sortKey, user);
    }

    public List<Meal> getMealsForUser(String userId) {
        return getMealsForUser(userId, null, null, null, "dateDesc", null);
    }

    public Meal findById(String mealId) {
        return mealRepository.findById(mealId);
    }

    public List<FoodItem> searchFoods(String keyword) {
        return foodCatalogRepository.search(keyword);
    }

    public FoodItem findFood(String code) {
        return foodCatalogRepository.findByCode(code);
    }

    public ServiceResult<Meal> createMeal(User user, LocalDate mealDate, String mealType, String memo, List<FoodItem> foods) {
        if (mealDate == null) {
            return ServiceResult.failure("식단 날짜를 선택해 주세요.");
        }
        if (foods == null || foods.isEmpty()) {
            return ServiceResult.failure("음식을 한 개 이상 선택해 주세요.");
        }

        Meal meal = new Meal();
        meal.setId(IdGenerator.next("meal"));
        meal.setUserId(user.getId());
        meal.setMealDate(mealDate);
        meal.setMealType(mealType);
        meal.setMemo(memo == null ? "" : memo.trim());
        meal.setFoods(sortFoodsByEnergy(foods));
        meal.setCreatedAt(LocalDateTime.now());
        meal.setUpdatedAt(LocalDateTime.now());
        mealRepository.save(meal);
        return ServiceResult.success("식단이 등록되었습니다.", meal);
    }

    public ServiceResult<Meal> updateMeal(User user, String mealId, LocalDate mealDate, String mealType, String memo, List<FoodItem> foods) {
        Meal meal = mealRepository.findById(mealId);
        if (meal == null || !user.getId().equals(meal.getUserId())) {
            return ServiceResult.failure("수정할 식단을 찾을 수 없습니다.");
        }
        if (mealDate == null) {
            return ServiceResult.failure("식단 날짜를 선택해 주세요.");
        }
        if (foods == null || foods.isEmpty()) {
            return ServiceResult.failure("음식을 한 개 이상 선택해 주세요.");
        }

        meal.setMealDate(mealDate);
        meal.setMealType(mealType);
        meal.setMemo(memo == null ? "" : memo.trim());
        meal.setFoods(sortFoodsByEnergy(foods));
        meal.setUpdatedAt(LocalDateTime.now());
        mealRepository.save(meal);
        return ServiceResult.success("식단이 수정되었습니다.", meal);
    }

    public void deleteMeal(User user, String mealId) {
        Meal meal = mealRepository.findById(mealId);
        if (meal != null && user.getId().equals(meal.getUserId())) {
            mealRepository.delete(mealId);
        }
    }

    public NutritionSummary summarize(List<FoodItem> foods) {
        NutritionSummary summary = new NutritionSummary();
        double calories = 0;
        double carbs = 0;
        double protein = 0;
        double fat = 0;
        for (FoodItem food : foods) {
            calories += food.getEnergy();
            carbs += food.getCarbs();
            protein += food.getProtein();
            fat += food.getFat();
        }
        summary.setCalories(round(calories));
        summary.setCarbs(round(carbs));
        summary.setProtein(round(protein));
        summary.setFat(round(fat));

        double macroCalories = (carbs * 4) + (protein * 4) + (fat * 9);
        if (macroCalories > 0) {
            summary.setCarbsPct((int) Math.round((carbs * 4 / macroCalories) * 100));
            summary.setProteinPct((int) Math.round((protein * 4 / macroCalories) * 100));
            summary.setFatPct((int) Math.round((fat * 9 / macroCalories) * 100));
        }
        return summary;
    }

    public DailyGoal calculateDailyGoal(User user) {
        int age = LocalDate.now().getYear() - user.getBirthYear() + 1;
        double bmr = "female".equals(user.getGender())
            ? 655.1 + (9.56 * user.getWeight()) + (1.85 * user.getHeight()) - (4.68 * age)
            : 66.47 + (13.75 * user.getWeight()) + (5.0 * user.getHeight()) - (6.76 * age);
        double calories = bmr * 1.45;
        if ("diet".equals(user.getGoal())) {
            calories -= 350;
        } else if ("muscle".equals(user.getGoal())) {
            calories += 250;
        }

        DailyGoal goal = new DailyGoal();
        goal.setCalories((int) Math.round(calories));
        goal.setCarbs((int) Math.round((calories * 0.5) / 4));
        goal.setProtein((int) Math.round((calories * 0.25) / 4));
        goal.setFat((int) Math.round((calories * 0.25) / 9));
        return goal;
    }

    public MealAnalysis analyzeMeal(Meal meal, User user) {
        NutritionSummary nutrition = summarize(meal.getFoods());
        DailyGoal goal = calculateDailyGoal(user);
        int mealGoalCalories = mealGoalCalories(goal, meal.getMealType());

        int score = 100;
        score -= Math.min(25, Math.abs((int) nutrition.getCalories() - mealGoalCalories) / 10);
        score -= Math.max(0, 20 - nutrition.getProteinPct());
        score -= Math.max(0, nutrition.getFatPct() - 35);
        score = Math.max(45, Math.min(100, score));

        String grade = score >= 90 ? "A" : score >= 80 ? "B" : score >= 70 ? "C" : "D";
        List<String> insights = new ArrayList<>();

        if (nutrition.getCalories() < mealGoalCalories * 0.75) {
            insights.add("현재 식사는 목표 칼로리보다 다소 낮습니다. 다음 식사에서 탄수화물이나 단백질을 조금 보강해 보세요.");
        } else if (nutrition.getCalories() > mealGoalCalories * 1.2) {
            insights.add("현재 식사는 목표 칼로리보다 높은 편입니다. 다음 식사는 조금 가볍게 조정하는 것이 좋습니다.");
        } else {
            insights.add("현재 식사는 목표 칼로리 범위 안에서 비교적 안정적입니다.");
        }
        if (nutrition.getProteinPct() < 20) {
            insights.add("단백질 비중이 낮습니다. 닭가슴살, 달걀, 두부, 그릭요거트 같은 식품을 더해 보세요.");
        }
        if (nutrition.getCarbsPct() > 58) {
            insights.add("탄수화물 비율이 높습니다. 다음 식사에서는 채소와 단백질 비중을 조금 늘리는 편이 좋습니다.");
        }
        if (nutrition.getFatPct() > 35) {
            insights.add("지방 비율이 높습니다. 견과류나 소스 양을 조금 줄여 보세요.");
        }

        MealAnalysis analysis = new MealAnalysis();
        analysis.setNutrition(nutrition);
        analysis.setScore(score);
        analysis.setGrade(grade);
        analysis.setInsights(insights);
        analysis.setHeadline(score >= 90 ? "영양 균형이 좋은 식단입니다." : score >= 80 ? "대체로 좋은 식단입니다." : "조금 더 균형을 맞춰보면 좋겠습니다.");
        analysis.setNextAction("diet".equals(user.getGoal())
            ? "다음 식사에서는 채소와 저지방 단백질 비중을 높여 보세요."
            : "muscle".equals(user.getGoal())
                ? "운동 후 3시간 안에 단백질 보강 식사를 챙겨 보세요."
                : "다음 식사도 비슷한 균형을 유지해 보세요.");
        return analysis;
    }

    public List<FoodRecommendation> recommendFoods(User user, String mealType, List<FoodItem> selectedFoods, int limit) {
        DailyGoal goal = calculateDailyGoal(user);
        int mealGoalCalories = mealGoalCalories(goal, mealType);
        int currentCalories = (int) Math.round(summarize(selectedFoods).getCalories());
        int remaining = Math.max(0, mealGoalCalories - currentCalories);

        Set<String> selectedCodes = new HashSet<>();
        for (FoodItem selected : selectedFoods) {
            selectedCodes.add(selected.getCode());
        }

        List<FoodRecommendation> candidates = new ArrayList<>();
        for (FoodItem food : foodCatalogRepository.findAll()) {
            if (selectedCodes.contains(food.getCode())) {
                continue;
            }
            FoodRecommendation recommendation = new FoodRecommendation();
            recommendation.setFood(food);
            recommendation.setEnergyGap(Math.abs((int) Math.round(food.getEnergy()) - remaining));
            candidates.add(recommendation);
        }

        List<FoodRecommendation> sorted = SortUtils.countingSort(candidates, FoodRecommendation::getEnergyGap);
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public List<FoodItem> sortFoodsByEnergy(List<FoodItem> foods) {
        return SortUtils.selectionSort(foods, (a, b) -> Double.compare(b.getEnergy(), a.getEnergy()));
    }

    public List<Meal> sortMeals(List<Meal> meals, String sortKey, User user) {
        final String resolvedSortKey = sortKey == null || sortKey.trim().isEmpty() ? "dateDesc" : sortKey;
        return SortUtils.quickSort(meals, (left, right) -> compareMeal(left, right, resolvedSortKey, user));
    }

    private int compareMeal(Meal left, Meal right, String sortKey, User user) {
        int dateCompare = left.getMealDate().compareTo(right.getMealDate());
        if ("dateAsc".equals(sortKey)) {
            if (dateCompare != 0) {
                return dateCompare;
            }
            return mealTypeRank(left.getMealType()) - mealTypeRank(right.getMealType());
        }

        if ("energyDesc".equals(sortKey)) {
            double leftEnergy = summarize(left.getFoods()).getCalories();
            double rightEnergy = summarize(right.getFoods()).getCalories();
            if (Double.compare(rightEnergy, leftEnergy) != 0) {
                return Double.compare(rightEnergy, leftEnergy);
            }
        } else if ("scoreDesc".equals(sortKey) && user != null) {
            int leftScore = analyzeMeal(left, user).getScore();
            int rightScore = analyzeMeal(right, user).getScore();
            if (rightScore != leftScore) {
                return rightScore - leftScore;
            }
        }

        if (dateCompare != 0) {
            return -dateCompare;
        }
        return mealTypeRank(left.getMealType()) - mealTypeRank(right.getMealType());
    }

    private int mealGoalCalories(DailyGoal goal, String mealType) {
        double ratio = "breakfast".equals(mealType) ? 0.28
            : "lunch".equals(mealType) ? 0.34
            : "dinner".equals(mealType) ? 0.28
            : 0.10;
        return (int) Math.round(goal.getCalories() * ratio);
    }

    private int mealTypeRank(String mealType) {
        if ("breakfast".equals(mealType)) {
            return 1;
        }
        if ("lunch".equals(mealType)) {
            return 2;
        }
        if ("dinner".equals(mealType)) {
            return 3;
        }
        return 4;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
