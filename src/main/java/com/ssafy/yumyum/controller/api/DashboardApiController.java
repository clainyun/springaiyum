package com.ssafy.yumyum.controller.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.meal.DailyGoalResponse;
import com.ssafy.yumyum.dto.meal.MealAnalysisResponse;
import com.ssafy.yumyum.dto.meal.MealNutritionResponse;
import com.ssafy.yumyum.dto.meal.MealSummaryResponse;
import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.CoachAdvice;
import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.MealAnalysis;
import com.ssafy.yumyum.model.NutritionSummary;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.model.WorkoutSession;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.ChallengeService;
import com.ssafy.yumyum.service.CoachService;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.util.SessionUtils;
import com.ssafy.yumyum.util.ViewHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Dashboard API", description = "홈/코치 대시보드 API")
public class DashboardApiController {

    private final UserRepository userRepository;
    private final MealService mealService;
    private final CoachService coachService;
    private final ChallengeService challengeService;
    private final SocialService socialService;

    public DashboardApiController(UserRepository userRepository,
                                  MealService mealService,
                                  CoachService coachService,
                                  ChallengeService challengeService,
                                  SocialService socialService) {
        this.userRepository = userRepository;
        this.mealService = mealService;
        this.coachService = coachService;
        this.challengeService = challengeService;
        this.socialService = socialService;
    }

    @GetMapping("/dashboard/home")
    @Operation(summary = "홈 대시보드 조회", description = "Vue 홈 화면에 필요한 요약 정보를 조회합니다.")
    public ResponseEntity<HomeDashboardResponse> home(HttpServletRequest request) {
        User user = getCurrentUser(request);
        List<Meal> meals = mealService.getMealsForUser(user.getId(), null, null, null, "dateDesc", user);
        List<Meal> recentMeals = meals.subList(0, Math.min(3, meals.size()));
        List<FoodItem> todayFoods = todayFoods(meals);
        NutritionSummary todaySummary = mealService.summarize(todayFoods);
        DailyGoal dailyGoal = mealService.calculateDailyGoal(user);
        CoachAdvice coachAdvice = coachService.buildAdvice(user);
        List<ChallengeSummary> activeChallenges = challengeService.getChallenges().stream()
                .limit(3)
                .map(ChallengeSummary::from)
                .toList();

        return ResponseEntity.ok(new HomeDashboardResponse(
                recentMeals.stream()
                        .map(meal -> MealSummaryResponse.from(meal, mealService.summarize(meal.getFoods())))
                        .toList(),
                MealNutritionResponse.from(todaySummary),
                DailyGoalResponse.from(dailyGoal),
                CoachSummary.from(coachAdvice),
                activeChallenges,
                socialService.countFollowing(user.getId()),
                socialService.countFollowers(user.getId())
        ));
    }

    @GetMapping("/coach/dashboard")
    @Operation(summary = "코치 대시보드 조회", description = "Vue 코치 화면에 필요한 코칭 정보를 조회합니다.")
    public ResponseEntity<CoachDashboardResponse> coach(HttpServletRequest request) {
        User user = getCurrentUser(request);
        List<Meal> meals = mealService.getMealsForUser(user.getId(), null, null, null, "dateDesc", user);
        CoachAdvice advice = coachService.buildAdvice(user);
        DailyGoal goal = mealService.calculateDailyGoal(user);
        NutritionSummary todaySummary = mealService.summarize(todayFoods(meals));
        int todayPct = goal.getCalories() == 0
                ? 0
                : (int) Math.round((todaySummary.getCalories() / goal.getCalories()) * 100);

        List<ChallengeCard> challenges = new ArrayList<>();
        for (ChallengeMembership membership : challengeService.membershipsForUser(user.getId())) {
            Challenge challenge = challengeService.findChallenge(membership.getChallengeId());
            challenges.add(new ChallengeCard(
                    membership.getChallengeId(),
                    challenge == null ? "챌린지" : challenge.getTitle(),
                    challenge == null ? "" : ViewHelper.nvl(challenge.getDescription(), ""),
                    membership.getProgress(),
                    challenge == null ? 0 : challenge.getTargetCount()
            ));
        }

        return ResponseEntity.ok(new CoachDashboardResponse(
                ViewHelper.nvl(advice.getSummary(), ""),
                ViewHelper.nvl(advice.getRecovery(), ""),
                new SummaryCard(todaySummary.getCalories(), todaySummary.getProtein()),
                new GoalCard(goal.getCalories(), goal.getProtein()),
                todayPct,
                advice.getSessions().stream().map(WorkoutSessionCard::from).toList(),
                new ArrayList<>(advice.getNextActions()),
                advice.getRecentAnalyses().stream().map(MealAnalysisCard::from).toList(),
                challenges
        ));
    }

    private List<FoodItem> todayFoods(List<Meal> meals) {
        List<FoodItem> todayFoods = new ArrayList<>();
        for (Meal meal : meals) {
            if (LocalDate.now().equals(meal.getMealDate())) {
                todayFoods.addAll(meal.getFoods());
            }
        }
        return todayFoods;
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

    public record HomeDashboardResponse(
            List<MealSummaryResponse> recentMeals,
            MealNutritionResponse todaySummary,
            DailyGoalResponse dailyGoal,
            CoachSummary coachAdvice,
            List<ChallengeSummary> activeChallenges,
            int followingCount,
            int followerCount
    ) {
    }

    public record CoachSummary(
            String summary,
            String recovery,
            List<MealAnalysisResponse> recentAnalyses
    ) {
        public static CoachSummary from(CoachAdvice advice) {
            return new CoachSummary(
                    ViewHelper.nvl(advice.getSummary(), ""),
                    ViewHelper.nvl(advice.getRecovery(), ""),
                    advice.getRecentAnalyses().stream().map(MealAnalysisResponse::from).toList()
            );
        }
    }

    public record ChallengeSummary(
            String id,
            String title,
            String description,
            int targetCount,
            String endDate
    ) {
        public static ChallengeSummary from(Challenge challenge) {
            return new ChallengeSummary(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getTargetCount(),
                    challenge.getEndDate() == null ? "" : challenge.getEndDate().toString()
            );
        }
    }

    public record CoachDashboardResponse(
            String summary,
            String recovery,
            SummaryCard todaySummary,
            GoalCard dailyGoal,
            int todayPct,
            List<WorkoutSessionCard> sessions,
            List<String> nextActions,
            List<MealAnalysisCard> recentAnalyses,
            List<ChallengeCard> challenges
    ) {
    }

    public record SummaryCard(double calories, double protein) {
    }

    public record GoalCard(int calories, int protein) {
    }

    public record WorkoutSessionCard(String title, String detail, String intensity) {
        public static WorkoutSessionCard from(WorkoutSession session) {
            return new WorkoutSessionCard(session.getTitle(), session.getDetail(), session.getIntensity());
        }
    }

    public record MealAnalysisCard(String headline, String nextAction, String grade, int score) {
        public static MealAnalysisCard from(MealAnalysis analysis) {
            return new MealAnalysisCard(
                    analysis.getHeadline(),
                    analysis.getNextAction(),
                    analysis.getGrade(),
                    analysis.getScore()
            );
        }
    }

    public record ChallengeCard(String id, String title, String description, int progress, int targetCount) {
    }
}
