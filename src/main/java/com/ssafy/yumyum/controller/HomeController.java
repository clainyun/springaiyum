package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.CoachAdvice;
import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.NutritionSummary;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.ChallengeService;
import com.ssafy.yumyum.service.CoachService;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final MealService mealService;
    private final CoachService coachService;
    private final ChallengeService challengeService;
    private final SocialService socialService;

    public HomeController(UserRepository userRepository,
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

    @GetMapping({"/legacy", "/legacy/home"})
    public String home(HttpServletRequest request, Model model) {
        User user = getLoginUser(request);

        List<Meal> meals = mealService.getMealsForUser(
                user.getId(),
                null,
                null,
                null,
                "dateDesc",
                user
        );

        List<Meal> recentMeals = meals.subList(0, Math.min(3, meals.size()));

        List<FoodItem> todayFoods = new ArrayList<>();

        for (Meal meal : meals) {
            if (LocalDate.now().equals(meal.getMealDate())) {
                todayFoods.addAll(meal.getFoods());
            }
        }

        NutritionSummary todaySummary = mealService.summarize(todayFoods);
        DailyGoal dailyGoal = mealService.calculateDailyGoal(user);

        CoachAdvice coachAdvice = coachService.buildAdvice(user);
        List<Challenge> challenges = challengeService.getChallenges();

        model.addAttribute("pageTitle", "대시보드");
        model.addAttribute("activeNav", "home");
        model.addAttribute("recentMeals", recentMeals);
        model.addAttribute("todaySummary", todaySummary);
        model.addAttribute("dailyGoal", dailyGoal);
        model.addAttribute("coachAdvice", coachAdvice);
        model.addAttribute("activeChallenges", challenges.subList(0, Math.min(3, challenges.size())));
        model.addAttribute("followingCount", socialService.countFollowing(user.getId()));
        model.addAttribute("followerCount", socialService.countFollowers(user.getId()));

        return "home/index";
    }

    private User getLoginUser(HttpServletRequest request) {
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
}
