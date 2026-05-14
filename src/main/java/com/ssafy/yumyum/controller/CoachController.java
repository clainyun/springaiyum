package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.CoachAdvice;
import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.MealAnalysis;
import com.ssafy.yumyum.model.NutritionSummary;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.model.WorkoutSession;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.SessionUtils;
import com.ssafy.yumyum.util.ViewHelper;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/coach")
public class CoachController {

    private static final String LOGIN_REDIRECT = "redirect:/auth/login";
    private static final String COACH_INDEX_VIEW = "coach/index";

    @ModelAttribute
    public void exposeFlash(HttpServletRequest req) {
        SessionUtils.exposeFlash(req);
    }

    @ModelAttribute("pageTitle")
    public String pageTitle() {
        return "AI 코치";
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "coach";
    }

    @ModelAttribute("currentUser")
    public User currentUser(
        @SessionAttribute(value = "loginUserId", required = false) String loginUserId,
        HttpServletRequest req
    ) {
        User user = AppContainer.getUserService().findById(loginUserId);
        if (user == null || !user.isActive()) {
            SessionUtils.flash(req.getSession(), "warning", "로그인이 필요한 메뉴입니다.");
            return null;
        }
        return user;
    }

    @GetMapping
    public String getCoach(@ModelAttribute("currentUser") User user) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }
        return COACH_INDEX_VIEW;
    }

    @GetMapping("/dashboard")
    @ResponseBody
    public ResponseEntity<?> getCoachDashboard(@ModelAttribute("currentUser") User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("AUTH_REQUIRED", "로그인이 필요한 메뉴입니다."));
        }

        List<Meal> meals = AppContainer.getMealService().getMealsForUser(user.getId(), null, null, null, "dateDesc", user);
        CoachAdvice advice = AppContainer.getCoachService().buildAdvice(user);
        DailyGoal goal = AppContainer.getMealService().calculateDailyGoal(user);

        List<com.ssafy.yumyum.model.FoodItem> todayFoods = new ArrayList<>();
        for (Meal meal : meals) {
            if (LocalDate.now().equals(meal.getMealDate())) {
                todayFoods.addAll(meal.getFoods());
            }
        }

        NutritionSummary todaySummary = AppContainer.getMealService().summarize(todayFoods);
        int todayPct = goal.getCalories() == 0 ? 0 : (int) Math.round((todaySummary.getCalories() / goal.getCalories()) * 100);

        List<ChallengeCard> challenges = new ArrayList<>();
        for (ChallengeMembership membership : AppContainer.getChallengeService().membershipsForUser(user.getId())) {
            Challenge challenge = AppContainer.getChallengeService().findChallenge(membership.getChallengeId());
            challenges.add(new ChallengeCard(
                membership.getChallengeId(),
                challenge == null ? "챌린지" : challenge.getTitle(),
                challenge == null ? "" : ViewHelper.nvl(challenge.getDescription(), ""),
                membership.getProgress(),
                challenge == null ? 0 : challenge.getTargetCount()
            ));
        }

        List<WorkoutSessionCard> sessions = new ArrayList<>();
        for (WorkoutSession session : advice.getSessions()) {
            sessions.add(new WorkoutSessionCard(session.getTitle(), session.getDetail(), session.getIntensity()));
        }

        List<MealAnalysisCard> recentAnalyses = new ArrayList<>();
        for (MealAnalysis analysis : advice.getRecentAnalyses()) {
            recentAnalyses.add(new MealAnalysisCard(
                analysis.getHeadline(),
                analysis.getNextAction(),
                analysis.getGrade(),
                analysis.getScore()
            ));
        }

        CoachDashboardResponse response = new CoachDashboardResponse(
            ViewHelper.nvl(advice.getSummary(), ""),
            ViewHelper.nvl(advice.getRecovery(), ""),
            new SummaryCard(todaySummary.getCalories(), todaySummary.getProtein()),
            new GoalCard(goal.getCalories(), goal.getProtein()),
            todayPct,
            sessions,
            new ArrayList<>(advice.getNextActions()),
            recentAnalyses,
            challenges
        );
        return ResponseEntity.ok(response);
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
    }

    public record MealAnalysisCard(String headline, String nextAction, String grade, int score) {
    }

    public record ChallengeCard(String id, String title, String description, int progress, int targetCount) {
    }

    public record ErrorResponse(String code, String message) {
    }
}
