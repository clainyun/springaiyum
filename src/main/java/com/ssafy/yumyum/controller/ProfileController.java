package com.ssafy.yumyum.controller;

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

@WebServlet("/profile")
public class ProfileController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }
        req.setAttribute("pageTitle", "프로필");
        req.setAttribute("activeNav", "profile");
        req.setAttribute("dailyGoal", AppContainer.getMealService().calculateDailyGoal(user));
        req.setAttribute("mealCount", AppContainer.getMealService().getMealsForUser(user.getId()).size());
        req.setAttribute("followingCount", AppContainer.getSocialService().countFollowing(user.getId()));
        req.setAttribute("followerCount", AppContainer.getSocialService().countFollowers(user.getId()));
        req.setAttribute("joinedChallengeCount", AppContainer.getChallengeService().countJoined(user.getId()));
        render(req, resp, "profile/index");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }

        String action = req.getParameter("action");
        if ("deactivate".equals(action)) {
            AppContainer.getUserService().deactivate(user);
            SessionUtils.logout(req.getSession(false));
            req.getSession(true);
            SessionUtils.flash(req.getSession(), "info", "계정을 비활성화했습니다.");
            redirect(req, resp, "/auth/login");
            return;
        }
        if ("delete".equals(action)) {
            AppContainer.getUserService().delete(user);
            SessionUtils.logout(req.getSession(false));
            req.getSession(true);
            SessionUtils.flash(req.getSession(), "info", "계정을 삭제했습니다.");
            redirect(req, resp, "/auth/login");
            return;
        }

        ServiceResult<User> result = AppContainer.getUserService().updateProfile(
            user,
            req.getParameter("email"),
            req.getParameter("nickname"),
            req.getParameter("password"),
            req.getParameter("gender"),
            parseInt(req.getParameter("birthYear"), user.getBirthYear()),
            parseDouble(req.getParameter("height"), user.getHeight()),
            parseDouble(req.getParameter("weight"), user.getWeight()),
            req.getParameter("goal"),
            req.getParameter("healthNote")
        );
        if (!result.isOk()) {
            req.setAttribute("errorMessage", result.getMessage());
        } else {
            SessionUtils.flash(req.getSession(), "success", result.getMessage());
            redirect(req, resp, "/profile");
            return;
        }
        doGet(req, resp);
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private double parseDouble(String raw, double fallback) {
        try {
            return Double.parseDouble(raw);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
