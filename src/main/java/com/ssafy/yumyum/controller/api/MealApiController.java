package com.ssafy.yumyum.controller.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.MealService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/meals")
@Tag(name = "Meal API", description = "식단 관리 API")
public class MealApiController {

    private final MealService mealService;
    private final UserRepository userRepository;

    public MealApiController(MealService mealService, UserRepository userRepository) {
        this.mealService = mealService;
        this.userRepository = userRepository;
    }
}
