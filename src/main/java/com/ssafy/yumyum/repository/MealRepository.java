package com.ssafy.yumyum.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ssafy.yumyum.model.Meal;

public class MealRepository {

    private final Map<String, Meal> meals = new LinkedHashMap<>();

    public MealRepository(List<Meal> seedMeals) {
        for (Meal meal : seedMeals) {
            meals.put(meal.getId(), meal);
        }
    }

    public synchronized List<Meal> findAll() {
        return new ArrayList<>(meals.values());
    }

    public synchronized Meal findById(String id) {
        return meals.get(id);
    }

    public synchronized void save(Meal meal) {
        meals.put(meal.getId(), meal);
    }

    public synchronized void delete(String mealId) {
        meals.remove(mealId);
    }
}
