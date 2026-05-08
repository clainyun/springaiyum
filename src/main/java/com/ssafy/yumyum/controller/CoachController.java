package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.CoachAdvice;
import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.NutritionSummary;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.BaseController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/coach")
public class CoachController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
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
        Map<String, Challenge> challengeMap = new HashMap<>();
        for (ChallengeMembership membership : AppContainer.getChallengeService().membershipsForUser(user.getId())) {
            Challenge challenge = AppContainer.getChallengeService().findChallenge(membership.getChallengeId());
            if (challenge != null) {
                challengeMap.put(challenge.getId(), challenge);
            }
        }

        req.setAttribute("pageTitle", "AI 코치");
        req.setAttribute("activeNav", "coach");
        req.setAttribute("coachAdvice", advice);
        req.setAttribute("todaySummary", todaySummary);
        req.setAttribute("dailyGoal", goal);
        req.setAttribute("memberships", AppContainer.getChallengeService().membershipsForUser(user.getId()));
        req.setAttribute("challengeMap", challengeMap);
        render(req, resp, "coach/index");
    }
}
