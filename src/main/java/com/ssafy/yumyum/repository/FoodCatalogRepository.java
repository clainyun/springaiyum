package com.ssafy.yumyum.repository;

import java.util.ArrayList;
import java.util.List;

import com.ssafy.yumyum.model.FoodItem;

public class FoodCatalogRepository {

    private final List<FoodItem> foods;

    public FoodCatalogRepository(List<FoodItem> foods) {
        this.foods = new ArrayList<>(foods);
    }

    public List<FoodItem> findAll() {
        return new ArrayList<>(foods);
    }

    public FoodItem findByCode(String code) {
        for (FoodItem food : foods) {
            if (food.getCode().equals(code)) {
                return food;
            }
        }
        return null;
    }

    public List<FoodItem> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String normalized = keyword.trim().toLowerCase();
        List<FoodItem> result = new ArrayList<>();
        for (FoodItem food : foods) {
            if (food.getName().toLowerCase().contains(normalized)
                || food.getCategory().toLowerCase().contains(normalized)) {
                result.add(food);
            }
        }
        return result;
    }
}
