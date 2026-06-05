package com.ssafy.yumyum.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.common.MessageResponse;
import com.ssafy.yumyum.dto.user.UpdateUserProfileRequest;
import com.ssafy.yumyum.dto.user.UserProfileDashboardResponse;
import com.ssafy.yumyum.dto.user.UserProfileResponse;
import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.service.ChallengeService;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.service.SocialService;
import com.ssafy.yumyum.service.UserService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User API", description = "사용자 프로필 API")
public class UserApiController {

    private final UserService userService;
    private final MealService mealService;
    private final SocialService socialService;
    private final ChallengeService challengeService;

    public UserApiController(UserService userService,
                             MealService mealService,
                             SocialService socialService,
                             ChallengeService challengeService) {
        this.userService = userService;
        this.mealService = mealService;
        this.socialService = socialService;
        this.challengeService = challengeService;
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    public ResponseEntity<UserProfileResponse> me(HttpServletRequest request) {
        return ResponseEntity.ok(UserProfileResponse.from(getCurrentUser(request)));
    }

    @GetMapping("/me/dashboard")
    @Operation(summary = "내 프로필 대시보드 조회", description = "프로필 화면에 필요한 목표와 활동 통계를 함께 조회합니다.")
    public ResponseEntity<UserProfileDashboardResponse> dashboard(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        return ResponseEntity.ok(UserProfileDashboardResponse.from(
                currentUser,
                mealService.calculateDailyGoal(currentUser),
                mealService.getMealsForUser(currentUser.getId()).size(),
                socialService.countFollowing(currentUser.getId()),
                socialService.countFollowers(currentUser.getId()),
                challengeService.countJoined(currentUser.getId())
        ));
    }

    @PutMapping("/me")
    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateUserProfileRequest request,
                                                             HttpServletRequest httpRequest) {
        User currentUser = getCurrentUser(httpRequest);
        ServiceResult<User> result = userService.updateProfile(
                currentUser,
                request.email(),
                request.nickname(),
                request.password(),
                request.gender(),
                request.birthYear() == null ? currentUser.getBirthYear() : request.birthYear(),
                request.height() == null ? currentUser.getHeight() : request.height(),
                request.weight() == null ? currentUser.getWeight() : request.weight(),
                request.goal(),
                request.healthNote()
        );
        if (!result.isOk()) {
            throw new CustomException(400, result.getMessage());
        }

        return ResponseEntity.ok(UserProfileResponse.from(result.getData()));
    }

    @PostMapping("/me/deactivate")
    @Operation(summary = "내 계정 비활성화", description = "로그인한 사용자의 계정을 비활성화하고 세션을 종료합니다.")
    public ResponseEntity<MessageResponse> deactivate(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        userService.deactivate(currentUser);
        SessionUtils.logout(request.getSession(false));
        return ResponseEntity.ok(new MessageResponse("계정이 비활성화되었습니다."));
    }

    @DeleteMapping("/me")
    @Operation(summary = "내 계정 영구 삭제", description = "로그인한 사용자의 계정과 관련 데이터를 삭제하고 세션을 종료합니다.")
    public ResponseEntity<MessageResponse> delete(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        userService.delete(currentUser);
        SessionUtils.logout(request.getSession(false));
        return ResponseEntity.ok(new MessageResponse("계정이 삭제되었습니다."));
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
}
