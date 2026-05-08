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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/social")
public class SocialController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }

        List<User> following = AppContainer.getSocialService().getFollowing(user.getId());
        List<User> followers = AppContainer.getSocialService().getFollowers(user.getId());
        List<User> suggestions = AppContainer.getSocialService().getSuggestions(user.getId(), 6);
        List<User> leaderboard = AppContainer.getSocialService().getLeaderboard(user.getId(), 5);
        List<User> countTargets = new ArrayList<>();
        countTargets.addAll(suggestions);
        countTargets.addAll(leaderboard);
        Map<String, Integer> followerCounts = AppContainer.getSocialService().followerCountMap(countTargets);
        Set<String> followingIds = new HashSet<>();
        for (User followingUser : following) {
            followingIds.add(followingUser.getId());
        }

        req.setAttribute("pageTitle", "소셜");
        req.setAttribute("activeNav", "social");
        req.setAttribute("following", following);
        req.setAttribute("followers", followers);
        req.setAttribute("suggestions", suggestions);
        req.setAttribute("leaderboard", leaderboard);
        req.setAttribute("followerCounts", followerCounts);
        req.setAttribute("followingIds", followingIds);
        render(req, resp, "social/index");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }

        String action = req.getParameter("action");
        String targetUserId = req.getParameter("targetUserId");
        if ("follow".equals(action)) {
            ServiceResult<Void> result = AppContainer.getSocialService().follow(user.getId(), targetUserId);
            SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        } else if ("unfollow".equals(action)) {
            AppContainer.getSocialService().unfollow(user.getId(), targetUserId);
            SessionUtils.flash(req.getSession(), "info", "팔로우를 해제했습니다.");
        }
        redirect(req, resp, "/social");
    }
}
