package com.ssafy.yumyum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.ChallengeService;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.service.UserService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final MealService mealService;
    private final SocialService socialService;
    private final ChallengeService challengeService;
    private final UserService userService;

    public ProfileController(UserRepository userRepository,
                             MealService mealService,
                             SocialService socialService,
                             ChallengeService challengeService,
                             UserService userService) {
        this.userRepository = userRepository;
        this.mealService = mealService;
        this.socialService = socialService;
        this.challengeService = challengeService;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        User user = getLoginUser(request);

        populateProfileModel(model, user);
        return "profile/index";
    }

    @PostMapping("/profile")
    public String updateProfile(HttpServletRequest request, Model model) {
        User user = getLoginUser(request);

        String action = request.getParameter("action");
        if ("deactivate".equals(action) || "delete".equals(action)) {
            user.setActive(false);
            userRepository.save(user);

            SessionUtils.logout(request.getSession(false));

            HttpSession newSession = request.getSession(true);
            SessionUtils.flash(newSession, "info", "계정이 비활성화되었습니다.");

            return "redirect:/auth/login";
        }

        ServiceResult<User> result = userService.updateProfile(
                user,
                request.getParameter("email"),
                request.getParameter("nickname"),
                request.getParameter("password"),
                request.getParameter("gender"),
                parseInt(request.getParameter("birthYear"), user.getBirthYear()),
                parseDouble(request.getParameter("height"), user.getHeight()),
                parseDouble(request.getParameter("weight"), user.getWeight()),
                request.getParameter("goal"),
                request.getParameter("healthNote")
        );

        if (!result.isOk()) {
            return profileWithError(model, user, result.getMessage());
        }

        SessionUtils.flash(request.getSession(), "success", "프로필을 수정했습니다.");
        return "redirect:/profile";
    }

    private String profileWithError(Model model, User user, String errorMessage) {
        populateProfileModel(model, user);
        model.addAttribute("errorMessage", errorMessage);
        return "profile/index";
    }

    private void populateProfileModel(Model model, User user) {
        model.addAttribute("pageTitle", "프로필");
        model.addAttribute("activeNav", "profile");
        model.addAttribute("currentUser", user);
        model.addAttribute("dailyGoal", mealService.calculateDailyGoal(user));
        model.addAttribute("mealCount", mealService.getMealsForUser(user.getId()).size());
        model.addAttribute("followingCount", socialService.countFollowing(user.getId()));
        model.addAttribute("followerCount", socialService.countFollowers(user.getId()));
        model.addAttribute("joinedChallengeCount", challengeService.countJoined(user.getId()));
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
