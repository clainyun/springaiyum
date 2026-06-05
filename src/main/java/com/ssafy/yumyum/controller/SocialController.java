package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.service.UserService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/legacy/social")
public class SocialController {

    private static final String LOGIN_REDIRECT = "redirect:/legacy/auth/login";
    private static final String SOCIAL_INDEX_VIEW = "social/index";
    private static final String SOCIAL_REDIRECT = "redirect:/legacy/social";

    private final UserService userService;
    private final SocialService socialService;

    public SocialController(UserService userService, SocialService socialService) {
        this.userService = userService;
        this.socialService = socialService;
    }

    @ModelAttribute
    public void exposeFlash(HttpServletRequest request) {
        SessionUtils.exposeFlash(request);
    }

    @ModelAttribute("pageTitle")
    public String pageTitle() {
        return "소셜";
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "social";
    }

    @ModelAttribute("currentUser")
    public User currentUser(
            @SessionAttribute(value = "loginUserId", required = false) String loginUserId,
            HttpServletRequest request
    ) {
        if (loginUserId == null) {
            SessionUtils.flash(request.getSession(), "warning", "로그인이 필요한 메뉴입니다.");
            return null;
        }

        User user = userService.findById(loginUserId);

        if (user == null || !user.isActive()) {
            SessionUtils.flash(request.getSession(), "warning", "로그인이 필요한 메뉴입니다.");
            return null;
        }

        return user;
    }

    @GetMapping
    public String getSocial(@ModelAttribute("currentUser") User user, Model model) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        List<User> following = socialService.getFollowing(user.getId());
        List<User> followers = socialService.getFollowers(user.getId());
        List<User> suggestions = socialService.getSuggestions(user.getId(), 6);
        List<User> leaderboard = socialService.getLeaderboard(user.getId(), 5);

        List<User> countTargets = new ArrayList<>();
        countTargets.addAll(suggestions);
        countTargets.addAll(leaderboard);

        Map<String, Integer> followerCounts = socialService.followerCountMap(countTargets);

        Set<String> followingIds = new HashSet<>();
        for (User followingUser : following) {
            followingIds.add(followingUser.getId());
        }

        model.addAttribute("following", following);
        model.addAttribute("followers", followers);
        model.addAttribute("suggestions", suggestions);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("followerCounts", followerCounts);
        model.addAttribute("followingIds", followingIds);

        return SOCIAL_INDEX_VIEW;
    }

    @PostMapping
    public String updateSocial(
            @ModelAttribute("currentUser") User user,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetUserId,
            HttpServletRequest request
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        if ("follow".equals(action)) {
            ServiceResult<Void> result = socialService.follow(user.getId(), targetUserId);
            SessionUtils.flash(request.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        } else if ("unfollow".equals(action)) {
            socialService.unfollow(user.getId(), targetUserId);
            SessionUtils.flash(request.getSession(), "info", "팔로우를 해제했습니다.");
        }

        return SOCIAL_REDIRECT;
    }
}
