package com.ssafy.yumyum.controller.api;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.tool.DailyGoalTool;
import com.ssafy.yumyum.tool.FoodSearchTool;
import com.ssafy.yumyum.tool.TodayMealTool;
import com.ssafy.yumyum.tool.UserProfileTool;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI API", description = "Spring AI 기반 음식 검색 및 영양 코치 API")
public class AiApiController {

    private final ChatClient chatClient;
    private final FoodSearchTool foodSearchTool;
    private final UserProfileTool userProfileTool;
    private final TodayMealTool todayMealTool;
    private final DailyGoalTool dailyGoalTool;

    public AiApiController(ChatClient chatClient,
                           FoodSearchTool foodSearchTool,
                           UserProfileTool userProfileTool,
                           TodayMealTool todayMealTool,
                           DailyGoalTool dailyGoalTool) {
        this.chatClient = chatClient;
        this.foodSearchTool = foodSearchTool;
        this.userProfileTool = userProfileTool;
        this.todayMealTool = todayMealTool;
        this.dailyGoalTool = dailyGoalTool;
    }

    @GetMapping("/food")
    @Operation(
        summary = "[F1203] 단일 Tool - 음식 영양 검색",
        description = "FoodSearchTool 단일 호출. 음식 이름으로 영양성분을 검색하고 AI가 응답합니다."
    )
    public ResponseEntity<Map<String, String>> foodSearch(
            @Parameter(description = "예: 닭가슴살 100g의 칼로리가 얼마야?")
            @RequestParam String question) {

        String answer = chatClient.prompt()
                .user(question)
                .tools(foodSearchTool)
                .call()
                .content();

        return ResponseEntity.ok(Map.of("answer", answer));
    }

    @GetMapping("/coach")
    @Operation(
        summary = "[F1204] 다중 Tool - 오늘 식단 분석",
        description = "UserProfileTool → TodayMealTool → DailyGoalTool 순서로 연쇄 호출 후 Gemini가 종합 분석을 생성합니다."
    )
    public ResponseEntity<Map<String, String>> coachAnalysis(
            @Parameter(description = "분석할 사용자 ID (Swagger 테스트 시 DB에 존재하는 userId 입력)")
            @RequestParam String userId,
            @Parameter(description = "예: 오늘 내 식단을 분석해줘")
            @RequestParam(defaultValue = "오늘 내 식단을 분석해줘") String question) {

        String userMessage = question + "\n(분석 대상 사용자 ID: " + userId + ")";

        String answer = chatClient.prompt()
                .user(userMessage)
                .tools(foodSearchTool, userProfileTool, todayMealTool, dailyGoalTool)
                .call()
                .content();

        return ResponseEntity.ok(Map.of("answer", answer));
    }
}
