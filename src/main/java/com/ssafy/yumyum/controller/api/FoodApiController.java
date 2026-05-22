package com.ssafy.yumyum.controller.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.service.MealService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/foods")
@Tag(name = "Food API", description = "식품 검색 API")
public class FoodApiController {

    private final MealService mealService;

    public FoodApiController(MealService mealService) {
        this.mealService = mealService;
    }
}
