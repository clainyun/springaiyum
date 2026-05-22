package com.ssafy.yumyum.controller.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.meal.MealDetailResponse;
import com.ssafy.yumyum.dto.meal.MealSummaryResponse;
import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.util.SessionUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

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

    @GetMapping
    @Operation(summary = "식단 목록 조회", description = "로그인한 사용자의 식단 목록을 조회합니다.")
    public ResponseEntity<List<MealSummaryResponse>> list(HttpServletRequest request,
                                                          @RequestParam(required = false) String startDate,
                                                          @RequestParam(required = false) String endDate,
                                                          @RequestParam(required = false) String mealType,
                                                          @RequestParam(required = false) String sortKey) {
        User user = getCurrentUser(request);
        List<MealSummaryResponse> meals = mealService.getMealsForUser(
                        user.getId(),
                        parseDate(startDate),
                        parseDate(endDate),
                        mealType,
                        valueOrDefault(sortKey, "dateDesc"),
                        user
                ).stream()
                .map(meal -> MealSummaryResponse.from(meal, mealService.summarize(meal.getFoods())))
                .toList();

        return ResponseEntity.ok(meals);
    }

    @GetMapping("/{mealId}")
    @Operation(summary = "식단 상세 조회", description = "로그인한 사용자의 특정 식단 상세 정보를 조회합니다.")
    public ResponseEntity<MealDetailResponse> detail(@PathVariable String mealId, HttpServletRequest request) {
        User user = getCurrentUser(request);
        Meal meal = mealService.findById(mealId);

        if (meal == null || !user.getId().equals(meal.getUserId())) {
            throw new CustomException(404, "식단을 찾을 수 없습니다.");
        }

        return ResponseEntity.ok(MealDetailResponse.from(meal, mealService.summarize(meal.getFoods())));
    }

    private User getCurrentUser(HttpServletRequest request) {
        String loginUserId = SessionUtils.currentUserId(request);
        if (loginUserId == null) {
            throw new CustomException(401, "로그인이 필요합니다.");
        }

        User user = userRepository.findById(loginUserId);
        if (user == null || !user.isActive()) {
            throw new CustomException(401, "로그인 정보를 찾을 수 없습니다.");
        }

        return user;
    }

    private LocalDate parseDate(String raw) {
        try {
            return raw == null || raw.trim().isEmpty() ? null : LocalDate.parse(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
