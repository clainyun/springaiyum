package com.ssafy.yumyum.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.MealService;

@Component
public class DailyGoalTool {

    private static final Logger log = LoggerFactory.getLogger(DailyGoalTool.class);

    private final UserRepository userRepository;
    private final MealService mealService;

    public DailyGoalTool(UserRepository userRepository, MealService mealService) {
        this.userRepository = userRepository;
        this.mealService = mealService;
    }

    @Tool(description = "사용자의 신체 정보(신장, 체중, 나이, 목표)를 바탕으로 BMR을 계산하여 하루 권장 영양 목표(칼로리, 탄수화물, 단백질, 지방)를 조회합니다.")
    public DailyGoalResult getDailyGoal(
            @ToolParam(description = "일일 영양 목표를 계산할 사용자의 ID") String userId) {

        log.info("[Tool 호출] DailyGoalTool.getDailyGoal - userId: {}", userId);

        User user = userRepository.findById(userId);
        if (user == null) {
            log.warn("[Tool 결과] DailyGoalTool - 사용자를 찾을 수 없음: {}", userId);
            return null;
        }

        DailyGoal goal = mealService.calculateDailyGoal(user);

        DailyGoalResult result = new DailyGoalResult(
                goal.getCalories(),
                goal.getCarbs(),
                goal.getProtein(),
                goal.getFat()
        );

        log.info("[Tool 결과] DailyGoalTool - 목표 칼로리: {}kcal, 탄수화물: {}g, 단백질: {}g, 지방: {}g",
                goal.getCalories(), goal.getCarbs(), goal.getProtein(), goal.getFat());

        return result;
    }

    public record DailyGoalResult(
            int targetCalories,
            int targetCarbs,
            int targetProtein,
            int targetFat
    ) {}
}
