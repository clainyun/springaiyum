package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.FoodItem;
import com.ssafy.yumyum.model.FoodRecommendation;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.MealAnalysis;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.BaseController;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet(urlPatterns = {"/meals", "/meals/new", "/meals/detail", "/meals/edit", "/meals/delete"})
public class MealController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }

        String path = req.getServletPath();
        if ("/meals".equals(path)) {
            renderList(req, resp, user);
            return;
        }
        if ("/meals/detail".equals(path)) {
            renderDetail(req, resp, user);
            return;
        }
        renderForm(req, resp, user, "/meals/edit".equals(path), null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }
        String path = req.getServletPath();
        if ("/meals/delete".equals(path)) {
            AppContainer.getMealService().deleteMeal(user, req.getParameter("mealId"));
            SessionUtils.flash(req.getSession(), "success", "식단을 삭제했습니다.");
            redirect(req, resp, "/meals");
            return;
        }
        if ("/meals/new".equals(path)) {
            handleCreate(req, resp, user);
            return;
        }
        handleUpdate(req, resp, user);
    }

    private void renderList(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        LocalDate startDate = parseDate(req.getParameter("startDate"));
        LocalDate endDate = parseDate(req.getParameter("endDate"));
        String mealType = req.getParameter("mealType");
        String sortKey = req.getParameter("sortKey");

        req.setAttribute("pageTitle", "식단 목록");
        req.setAttribute("activeNav", "diet");
        req.setAttribute("meals", AppContainer.getMealService().getMealsForUser(user.getId(), startDate, endDate, mealType, sortKey, user));
        req.setAttribute("sortKey", sortKey == null || sortKey.isEmpty() ? "dateDesc" : sortKey);
        req.setAttribute("filterStart", req.getParameter("startDate"));
        req.setAttribute("filterEnd", req.getParameter("endDate"));
        req.setAttribute("filterMealType", mealType);
        render(req, resp, "meal/list");
    }

    private void renderDetail(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        Meal meal = AppContainer.getMealService().findById(req.getParameter("mealId"));
        if (meal == null || !user.getId().equals(meal.getUserId())) {
            SessionUtils.flash(req.getSession(), "warning", "식단을 찾을 수 없습니다.");
            redirect(req, resp, "/meals");
            return;
        }
        MealAnalysis analysis = AppContainer.getMealService().analyzeMeal(meal, user);
        req.setAttribute("pageTitle", "식단 상세");
        req.setAttribute("activeNav", "diet");
        req.setAttribute("meal", meal);
        req.setAttribute("analysis", analysis);
        req.setAttribute("dailyGoal", AppContainer.getMealService().calculateDailyGoal(user));
        render(req, resp, "meal/detail");
    }

    private void renderForm(HttpServletRequest req, HttpServletResponse resp, User user, boolean editMode,
                            Meal existingMeal, String errorMessage) throws ServletException, IOException {
        Meal meal = existingMeal;
        if (editMode && meal == null) {
            meal = AppContainer.getMealService().findById(req.getParameter("mealId"));
            if (meal == null || !user.getId().equals(meal.getUserId())) {
                SessionUtils.flash(req.getSession(), "warning", "식단을 찾을 수 없습니다.");
                redirect(req, resp, "/meals");
                return;
            }
        }

        String keyword = req.getParameter("keyword");
        List<FoodItem> selectedFoods = meal == null ? new ArrayList<>() : meal.getFoods();
        List<FoodRecommendation> recommendations = AppContainer.getMealService().recommendFoods(
            user,
            meal == null ? valueOrDefault(req.getParameter("mealType"), "lunch") : meal.getMealType(),
            selectedFoods,
            6
        );

        req.setAttribute("pageTitle", editMode ? "식단 수정" : "식단 등록");
        req.setAttribute("activeNav", "diet");
        req.setAttribute("editMode", editMode);
        req.setAttribute("meal", meal);
        req.setAttribute("errorMessage", errorMessage);
        req.setAttribute("catalogFoods", AppContainer.getMealService().searchFoods(keyword));
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedCodeSet", selectedCodeSet(selectedFoods));
        req.setAttribute("selectedFoods", selectedFoods);
        req.setAttribute("recommendations", recommendations);
        render(req, resp, "meal/form");
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        List<FoodItem> selectedFoods = selectedFoods(req);
        ServiceResult<Meal> result = AppContainer.getMealService().createMeal(
            user,
            parseDate(req.getParameter("mealDate")),
            valueOrDefault(req.getParameter("mealType"), "lunch"),
            req.getParameter("memo"),
            selectedFoods
        );
        if (!result.isOk()) {
            Meal tempMeal = new Meal();
            tempMeal.setMealDate(parseDate(req.getParameter("mealDate")));
            tempMeal.setMealType(valueOrDefault(req.getParameter("mealType"), "lunch"));
            tempMeal.setMemo(req.getParameter("memo"));
            tempMeal.setFoods(selectedFoods);
            renderForm(req, resp, user, false, tempMeal, result.getMessage());
            return;
        }
        SessionUtils.flash(req.getSession(), "success", result.getMessage());
        redirect(req, resp, "/meals");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        String mealId = req.getParameter("mealId");
        List<FoodItem> selectedFoods = selectedFoods(req);
        ServiceResult<Meal> result = AppContainer.getMealService().updateMeal(
            user,
            mealId,
            parseDate(req.getParameter("mealDate")),
            valueOrDefault(req.getParameter("mealType"), "lunch"),
            req.getParameter("memo"),
            selectedFoods
        );
        if (!result.isOk()) {
            Meal tempMeal = AppContainer.getMealService().findById(mealId);
            if (tempMeal == null) {
                tempMeal = new Meal();
                tempMeal.setId(mealId);
            }
            tempMeal.setMealDate(parseDate(req.getParameter("mealDate")));
            tempMeal.setMealType(valueOrDefault(req.getParameter("mealType"), "lunch"));
            tempMeal.setMemo(req.getParameter("memo"));
            tempMeal.setFoods(selectedFoods);
            renderForm(req, resp, user, true, tempMeal, result.getMessage());
            return;
        }
        SessionUtils.flash(req.getSession(), "success", result.getMessage());
        redirect(req, resp, "/meals/detail?mealId=" + result.getData().getId());
    }

    private List<FoodItem> selectedFoods(HttpServletRequest req) {
        List<FoodItem> foods = new ArrayList<>();
        String[] codes = req.getParameterValues("foodCode");
        if (codes == null) {
            return foods;
        }
        for (String code : codes) {
            FoodItem base = AppContainer.getMealService().findFood(code);
            if (base == null) {
                continue;
            }
            double grams = parseDouble(req.getParameter("grams_" + code), 100);
            foods.add(base.copyWithGrams(grams));
        }
        return foods;
    }

    private Set<String> selectedCodeSet(List<FoodItem> foods) {
        Set<String> selected = new HashSet<>();
        for (FoodItem food : foods) {
            selected.add(food.getCode());
        }
        return selected;
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
