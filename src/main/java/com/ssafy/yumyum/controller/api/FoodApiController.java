package com.ssafy.yumyum.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.food.FoodResponse;
import com.ssafy.yumyum.service.MealService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/foods")
@Tag(name = "Food API", description = "식품 검색 API")
public class FoodApiController {

    private final MealService mealService;

    public FoodApiController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping
    @Operation(summary = "식품 검색", description = "키워드로 식품 카탈로그를 조회합니다. 키워드가 없으면 전체 목록을 반환합니다.")
    public ResponseEntity<List<FoodResponse>> search(@RequestParam(required = false) String keyword) {
        List<FoodResponse> foods = mealService.searchFoods(keyword).stream()
                .map(FoodResponse::from)
                .toList();
        return ResponseEntity.ok(foods);
    }
}
