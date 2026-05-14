package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.CoachAdvice;
import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.NutritionSummary;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String getCoach(@ModelAttribute("currentUser") User user, Model model) {
        if (user == null) {
            return LOGIN_REDIRECT;
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
        List<ChallengeMembership> memberships = AppContainer.getChallengeService().membershipsForUser(user.getId());
        Map<String, Challenge> challengeMap = new HashMap<>();
        for (ChallengeMembership membership : memberships) {
            Challenge challenge = AppContainer.getChallengeService().findChallenge(membership.getChallengeId());
            if (challenge != null) {
                challengeMap.put(challenge.getId(), challenge);
            }
        }

        model.addAttribute("coachAdvice", advice);
        model.addAttribute("todaySummary", todaySummary);
        model.addAttribute("dailyGoal", goal);
        model.addAttribute("todayPct", todayPct);
        model.addAttribute("memberships", memberships);
        model.addAttribute("challengeMap", challengeMap);
        return COACH_INDEX_VIEW;
    }
}
