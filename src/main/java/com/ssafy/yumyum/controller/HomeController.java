package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.Challenge;
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
import java.util.List;

@WebServlet("/home")
public class HomeController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }

        List<Meal> meals = AppContainer.getMealService().getMealsForUser(user.getId(), null, null, null, "dateDesc", user);
        List<Meal> recentMeals = meals.subList(0, Math.min(3, meals.size()));
        List<com.ssafy.yumyum.model.FoodItem> todayFoods = new ArrayList<>();
        for (Meal meal : meals) {
            if (LocalDate.now().equals(meal.getMealDate())) {
                todayFoods.addAll(meal.getFoods());
            }
        }
        NutritionSummary todaySummary = AppContainer.getMealService().summarize(todayFoods);
        DailyGoal goal = AppContainer.getMealService().calculateDailyGoal(user);
        CoachAdvice coachAdvice = AppContainer.getCoachService().buildAdvice(user);
        List<Challenge> challenges = AppContainer.getChallengeService().getChallenges();

        req.setAttribute("pageTitle", "대시보드");
        req.setAttribute("activeNav", "home");
        req.setAttribute("recentMeals", recentMeals);
        req.setAttribute("todaySummary", todaySummary);
        req.setAttribute("dailyGoal", goal);
        req.setAttribute("coachAdvice", coachAdvice);
        req.setAttribute("activeChallenges", challenges.subList(0, Math.min(3, challenges.size())));
        req.setAttribute("followingCount", AppContainer.getSocialService().countFollowing(user.getId()));
        req.setAttribute("followerCount", AppContainer.getSocialService().countFollowers(user.getId()));
        render(req, resp, "home/index");
    }
}
