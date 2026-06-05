package com.ssafy.yumyum.controller.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.common.MessageResponse;
import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.service.UserService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;
import com.ssafy.yumyum.util.ViewHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/social")
@Tag(name = "Social API", description = "소셜 팔로우 API")
public class SocialApiController {

    private final UserService userService;
    private final SocialService socialService;

    public SocialApiController(UserService userService, SocialService socialService) {
        this.userService = userService;
        this.socialService = socialService;
    }

    @GetMapping
    @Operation(summary = "소셜 대시보드 조회")
    public ResponseEntity<SocialDashboardResponse> dashboard(HttpServletRequest request) {
        User user = getCurrentUser(request);
        List<User> following = socialService.getFollowing(user.getId());
        List<User> followers = socialService.getFollowers(user.getId());
        List<User> suggestions = socialService.getSuggestions(user.getId(), 6);
        List<User> leaderboard = socialService.getLeaderboard(user.getId(), 5);
        Set<String> followingIds = new HashSet<>();
        for (User followingUser : following) {
            followingIds.add(followingUser.getId());
        }

        return ResponseEntity.ok(new SocialDashboardResponse(
                userCards(suggestions, followingIds),
                userCards(following, followingIds),
                userCards(followers, followingIds),
                userCards(leaderboard, followingIds)
        ));
    }

    @PostMapping("/following/{targetUserId}")
    @Operation(summary = "팔로우")
    public ResponseEntity<MessageResponse> follow(@PathVariable String targetUserId, HttpServletRequest request) {
        ServiceResult<Void> result = socialService.follow(getCurrentUser(request).getId(), targetUserId);
        if (!result.isOk()) {
            throw new CustomException(400, result.getMessage());
        }
        return ResponseEntity.ok(new MessageResponse(result.getMessage()));
    }

    @DeleteMapping("/following/{targetUserId}")
    @Operation(summary = "언팔로우")
    public ResponseEntity<MessageResponse> unfollow(@PathVariable String targetUserId, HttpServletRequest request) {
        socialService.unfollow(getCurrentUser(request).getId(), targetUserId);
        return ResponseEntity.ok(new MessageResponse("팔로우를 해제했습니다."));
    }

    private List<SocialUserCard> userCards(List<User> users, Set<String> followingIds) {
        List<SocialUserCard> result = new ArrayList<>();
        for (User user : users) {
            result.add(new SocialUserCard(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getGoal(),
                    ViewHelper.goalLabel(user.getGoal()),
                    ViewHelper.goalShortLabel(user.getGoal()),
                    socialService.countFollowers(user.getId()),
                    followingIds.contains(user.getId())
            ));
        }
        return result;
    }

    private User getCurrentUser(HttpServletRequest request) {
        String loginUserId = SessionUtils.currentUserId(request);
        if (loginUserId == null) {
            throw new CustomException(401, "로그인이 필요합니다.");
        }
        User user = userService.findById(loginUserId);
        if (user == null || !user.isActive()) {
            throw new CustomException(401, "로그인 정보를 찾을 수 없습니다.");
        }
        return user;
    }

    public record SocialDashboardResponse(
            List<SocialUserCard> suggestions,
            List<SocialUserCard> following,
            List<SocialUserCard> followers,
            List<SocialUserCard> leaderboard
    ) {
    }

    public record SocialUserCard(String id, String email, String nickname, String goal, String goalLabel,
                                 String goalShortLabel, int followerCount, boolean following) {
    }
}
