package com.ssafy.yumyum.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.CoachAdvice;
import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.MealAnalysis;
import com.ssafy.yumyum.model.NutritionSummary;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.model.WorkoutSession;

import org.springframework.stereotype.Service;

@Service
public class CoachService {

    private final MealService mealService;
    private final ChallengeService challengeService;

    public CoachService(MealService mealService, ChallengeService challengeService) {
        this.mealService = mealService;
        this.challengeService = challengeService;
    }

    public CoachAdvice buildAdvice(User user) {
        List<Meal> meals = mealService.getMealsForUser(user.getId(), null, null, null, "dateDesc", user);
        List<Meal> recentMeals = meals.subList(0, Math.min(3, meals.size()));
        List<MealAnalysis> analyses = new ArrayList<>();
        for (Meal meal : recentMeals) {
            analyses.add(mealService.analyzeMeal(meal, user));
        }

        NutritionSummary todayNutrition = mealService.summarize(mealsForDate(meals, LocalDate.now()));
        DailyGoal goal = mealService.calculateDailyGoal(user);
        double calorieRatio = goal.getCalories() == 0 ? 0 : todayNutrition.getCalories() / goal.getCalories();

        CoachAdvice advice = new CoachAdvice();
        advice.setRecentMeals(new ArrayList<>(recentMeals));
        advice.setRecentAnalyses(analyses);
        advice.setSessions(buildSessions(user));
        advice.setSummary(calorieRatio < 0.6
            ? "오늘 섭취량이 아직 낮습니다. 운동 강도는 무리하지 않는 편이 좋습니다."
            : calorieRatio > 1.05
                ? "오늘 섭취량이 높은 편입니다. 저녁 이후에는 가벼운 활동 위주로 유지해 보세요."
                : "오늘 섭취량은 비교적 안정적입니다. 계획한 운동을 진행하기 좋습니다.");
        advice.setRecovery(todayNutrition.getProtein() < goal.getProtein() * 0.7
            ? "단백질 섭취가 부족합니다. 운동 후 보강 간식을 추가하는 편이 좋습니다."
            : "단백질 섭취는 무난합니다. 수분 보충과 수면 회복을 같이 챙겨 보세요.");

        List<String> nextActions = new ArrayList<>();
        if (todayNutrition.getProtein() < goal.getProtein() * 0.75) {
            nextActions.add("오늘 남은 시간에 단백질이 높은 식사나 간식을 한 번 추가해 보세요.");
        }
        if (todayNutrition.getCalories() < goal.getCalories() * 0.6) {
            nextActions.add("총 섭취량이 올라올 때까지 운동 강도는 중간 수준으로 유지하세요.");
        }
        if (todayNutrition.getCarbsPct() > 58) {
            nextActions.add("다음 식사에서는 탄수화물보다 채소와 단백질 비중을 조금 더 높여 보세요.");
        }
        if (nextActions.isEmpty()) {
            nextActions.add("현재 영양 균형은 안정적입니다. 꾸준함을 유지해 보세요.");
        }

        for (ChallengeMembership membership : challengeService.membershipsForUser(user.getId())) {
            Challenge challenge = challengeService.findChallenge(membership.getChallengeId());
            if (challenge != null) {
                nextActions.add("진행 중인 '" + challenge.getTitle() + "' 챌린지와 오늘 계획을 같이 점검해 보세요.");
                break;
            }
        }
        advice.setNextActions(nextActions);
        return advice;
    }

    private List<WorkoutSession> buildSessions(User user) {
        List<WorkoutSession> sessions = new ArrayList<>();
        if ("muscle".equals(user.getGoal())) {
            sessions.add(session("근력 운동", "복합 관절 위주의 45분 근력 운동을 권장합니다.", "중간~높음"));
            sessions.add(session("회복 걷기", "식후 10분 가벼운 걷기로 소화와 회복을 돕습니다.", "낮음"));
        } else if ("diet".equals(user.getGoal())) {
            sessions.add(session("유산소 운동", "빠르게 걷기 또는 사이클 30분을 추천합니다.", "중간"));
            sessions.add(session("전신 순환 운동", "스쿼트와 플랭크 중심의 전신 루틴을 20분 진행해 보세요.", "중간"));
        } else {
            sessions.add(session("가벼운 활동", "스트레칭과 걷기를 중심으로 컨디션을 유지해 보세요.", "낮음"));
            sessions.add(session("코어 루틴", "데드버그와 브릿지 중심으로 15분 루틴을 진행합니다.", "낮음"));
        }
        return sessions;
    }

    private WorkoutSession session(String title, String detail, String intensity) {
        WorkoutSession session = new WorkoutSession();
        session.setTitle(title);
        session.setDetail(detail);
        session.setIntensity(intensity);
        return session;
    }

    private List<com.ssafy.yumyum.model.FoodItem> mealsForDate(List<Meal> meals, LocalDate targetDate) {
        List<com.ssafy.yumyum.model.FoodItem> foods = new ArrayList<>();
        for (Meal meal : meals) {
            if (targetDate.equals(meal.getMealDate())) {
                foods.addAll(meal.getFoods());
            }
        }
        return foods;
    }
}
