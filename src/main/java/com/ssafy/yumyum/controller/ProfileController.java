package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.ChallengeService;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final MealService mealService;
    private final SocialService socialService;
    private final ChallengeService challengeService;

    public ProfileController(UserRepository userRepository,
                             MealService mealService,
                             SocialService socialService,
                             ChallengeService challengeService) {
        this.userRepository = userRepository;
        this.mealService = mealService;
        this.socialService = socialService;
        this.challengeService = challengeService;
    }

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        User user = getLoginUser(request);

        model.addAttribute("pageTitle", "프로필");
        model.addAttribute("activeNav", "profile");
        model.addAttribute("currentUser", user);
        model.addAttribute("dailyGoal", mealService.calculateDailyGoal(user));
        model.addAttribute("mealCount", mealService.getMealsForUser(user.getId()).size());
        model.addAttribute("followingCount", socialService.countFollowing(user.getId()));
        model.addAttribute("followerCount", socialService.countFollowers(user.getId()));
        model.addAttribute("joinedChallengeCount", challengeService.countJoined(user.getId()));

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
            SessionUtils.flash(newSession, "info", "계정을 비활성화했습니다.");

            return "redirect:/auth/login";
        }

        String email = request.getParameter("email");
        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");

        if (email == null || !email.contains("@")) {
            return profileWithError(request, model, user, "올바른 이메일을 입력해 주세요.");
        }

        if (nickname == null || nickname.trim().isEmpty()) {
            return profileWithError(request, model, user, "닉네임을 입력해 주세요.");
        }

        String trimmedEmail = email.trim();

        User duplicated = userRepository.findByEmail(trimmedEmail);

        if (duplicated != null && !duplicated.getId().equals(user.getId())) {
            return profileWithError(request, model, user, "이미 사용 중인 이메일입니다.");
        }

        user.setEmail(trimmedEmail);
        user.setNickname(nickname.trim());

        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 8) {
                return profileWithError(request, model, user, "비밀번호는 8자 이상이어야 합니다.");
            }

            user.setPassword(password);
        }

        user.setGender(request.getParameter("gender"));
        user.setBirthYear(parseInt(request.getParameter("birthYear"), user.getBirthYear()));
        user.setHeight(parseDouble(request.getParameter("height"), user.getHeight()));
        user.setWeight(parseDouble(request.getParameter("weight"), user.getWeight()));
        user.setGoal(request.getParameter("goal"));
        user.setHealthNote(request.getParameter("healthNote") == null ? "" : request.getParameter("healthNote").trim());

        userRepository.save(user);

        SessionUtils.flash(request.getSession(), "success", "프로필을 수정했습니다.");

        return "redirect:/profile";
    }

    private String profileWithError(HttpServletRequest request, Model model, User user, String errorMessage) {
        model.addAttribute("pageTitle", "프로필");
        model.addAttribute("activeNav", "profile");
        model.addAttribute("currentUser", user);
        model.addAttribute("dailyGoal", mealService.calculateDailyGoal(user));
        model.addAttribute("mealCount", mealService.getMealsForUser(user.getId()).size());
        model.addAttribute("followingCount", socialService.countFollowing(user.getId()));
        model.addAttribute("followerCount", socialService.countFollowers(user.getId()));
        model.addAttribute("joinedChallengeCount", challengeService.countJoined(user.getId()));
        model.addAttribute("errorMessage", errorMessage);

        return "profile/index";
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
