package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.model.FoodRecommendation;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.MealAnalysis;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/legacy/meals")
public class MealController {

    private final MealService mealService;
    private final UserRepository userRepository;

    public MealController(MealService mealService, UserRepository userRepository) {
        this.mealService = mealService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(HttpServletRequest request,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) String mealType,
                       @RequestParam(required = false) String sortKey,
                       Model model) {

        User user = getLoginUser(request);

        LocalDate parsedStartDate = parseDate(startDate);
        LocalDate parsedEndDate = parseDate(endDate);
        String resolvedSortKey = valueOrDefault(sortKey, "dateDesc");

        model.addAttribute("pageTitle", "식단 목록");
        model.addAttribute("activeNav", "diet");
        model.addAttribute("meals", mealService.getMealsForUser(
                user.getId(),
                parsedStartDate,
                parsedEndDate,
                mealType,
                resolvedSortKey,
                user
        ));
        model.addAttribute("sortKey", resolvedSortKey);
        model.addAttribute("filterStart", startDate);
        model.addAttribute("filterEnd", endDate);
        model.addAttribute("filterMealType", mealType);

        return "meal/list";
    }

    @GetMapping("/detail")
    public String detail(HttpServletRequest request,
                         @RequestParam String mealId,
                         Model model) {

        User user = getLoginUser(request);

        Meal meal = mealService.findById(mealId);

        if (meal == null || !user.getId().equals(meal.getUserId())) {
            SessionUtils.flash(request.getSession(), "warning", "식단을 찾을 수 없습니다.");
            return "redirect:/legacy/meals";
        }

        MealAnalysis analysis = mealService.analyzeMeal(meal, user);

        model.addAttribute("pageTitle", "식단 상세");
        model.addAttribute("activeNav", "diet");
        model.addAttribute("meal", meal);
        model.addAttribute("analysis", analysis);
        model.addAttribute("dailyGoal", mealService.calculateDailyGoal(user));

        return "meal/detail";
    }

    @GetMapping("/new")
    public String createForm(HttpServletRequest request,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String mealType,
                             Model model) {

        User user = getLoginUser(request);

        Meal tempMeal = new Meal();
        tempMeal.setMealType(valueOrDefault(mealType, "lunch"));

        return renderForm(user, false, tempMeal, keyword, null, model);
    }

    @GetMapping("/edit")
    public String editForm(HttpServletRequest request,
                           @RequestParam String mealId,
                           @RequestParam(required = false) String keyword,
                           Model model) {

        User user = getLoginUser(request);

        Meal meal = mealService.findById(mealId);

        if (meal == null || !user.getId().equals(meal.getUserId())) {
            SessionUtils.flash(request.getSession(), "warning", "식단을 찾을 수 없습니다.");
            return "redirect:/legacy/meals";
        }

        return renderForm(user, true, meal, keyword, null, model);
    }

    @PostMapping("/new")
    public String create(HttpServletRequest request, Model model) {
        User user = getLoginUser(request);

        List<FoodItem> selectedFoods = selectedFoods(request);

        ServiceResult<Meal> result = mealService.createMeal(
                user,
                parseDate(request.getParameter("mealDate")),
                valueOrDefault(request.getParameter("mealType"), "lunch"),
                request.getParameter("memo"),
                selectedFoods
        );

        if (!result.isOk()) {
            Meal tempMeal = new Meal();
            tempMeal.setMealDate(parseDate(request.getParameter("mealDate")));
            tempMeal.setMealType(valueOrDefault(request.getParameter("mealType"), "lunch"));
            tempMeal.setMemo(request.getParameter("memo"));
            tempMeal.setFoods(selectedFoods);

            return renderForm(user, false, tempMeal, request.getParameter("keyword"), result.getMessage(), model);
        }

        SessionUtils.flash(request.getSession(), "success", result.getMessage());

        return "redirect:/legacy/meals";
    }

    @PostMapping("/edit")
    public String update(HttpServletRequest request, Model model) {
        User user = getLoginUser(request);

        String mealId = request.getParameter("mealId");
        List<FoodItem> selectedFoods = selectedFoods(request);

        ServiceResult<Meal> result = mealService.updateMeal(
                user,
                mealId,
                parseDate(request.getParameter("mealDate")),
                valueOrDefault(request.getParameter("mealType"), "lunch"),
                request.getParameter("memo"),
                selectedFoods
        );

        if (!result.isOk()) {
            Meal tempMeal = mealService.findById(mealId);

            if (tempMeal == null) {
                tempMeal = new Meal();
                tempMeal.setId(mealId);
            }

            tempMeal.setMealDate(parseDate(request.getParameter("mealDate")));
            tempMeal.setMealType(valueOrDefault(request.getParameter("mealType"), "lunch"));
            tempMeal.setMemo(request.getParameter("memo"));
            tempMeal.setFoods(selectedFoods);

            return renderForm(user, true, tempMeal, request.getParameter("keyword"), result.getMessage(), model);
        }

        SessionUtils.flash(request.getSession(), "success", result.getMessage());

        return "redirect:/legacy/meals/detail?mealId=" + result.getData().getId();
    }

    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @RequestParam String mealId) {

        User user = getLoginUser(request);

        mealService.deleteMeal(user, mealId);
        SessionUtils.flash(request.getSession(), "success", "식단을 삭제했습니다.");

        return "redirect:/legacy/meals";
    }

    private String renderForm(User user,
                              boolean editMode,
                              Meal meal,
                              String keyword,
                              String errorMessage,
                              Model model) {

        List<FoodItem> selectedFoods = meal == null || meal.getFoods() == null
                ? new ArrayList<>()
                : meal.getFoods();

        String mealType = meal == null
                ? "lunch"
                : valueOrDefault(meal.getMealType(), "lunch");

        List<FoodRecommendation> recommendations = mealService.recommendFoods(
                user,
                mealType,
                selectedFoods,
                6
        );

        model.addAttribute("pageTitle", editMode ? "식단 수정" : "식단 등록");
        model.addAttribute("activeNav", "diet");
        model.addAttribute("editMode", editMode);
        model.addAttribute("meal", meal);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("catalogFoods", mealService.searchFoods(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCodeSet", selectedCodeSet(selectedFoods));
        model.addAttribute("selectedFoods", selectedFoods);
        model.addAttribute("recommendations", recommendations);

        return "meal/form";
    }

    private List<FoodItem> selectedFoods(HttpServletRequest request) {
        List<FoodItem> foods = new ArrayList<>();

        String[] codes = request.getParameterValues("foodCode");

        if (codes == null) {
            return foods;
        }

        for (String code : codes) {
            FoodItem base = mealService.findFood(code);

            if (base == null) {
                continue;
            }

            double grams = parseDouble(request.getParameter("grams_" + code), 100);
            foods.add(base.copyWithGrams(grams));
        }

        return foods;
    }

    private Set<String> selectedCodeSet(List<FoodItem> foods) {
        Set<String> selected = new HashSet<>();

        if (foods == null) {
            return selected;
        }

        for (FoodItem food : foods) {
            selected.add(food.getCode());
        }

        return selected;
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

    private LocalDate parseDate(String raw) {
        try {
            return raw == null || raw.trim().isEmpty() ? null : LocalDate.parse(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private double parseDouble(String raw, double fallback) {
        try {
            return Double.parseDouble(raw);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
