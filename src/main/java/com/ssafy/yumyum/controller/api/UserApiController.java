package com.ssafy.yumyum.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.user.UpdateUserProfileRequest;
import com.ssafy.yumyum.dto.user.UserProfileResponse;
import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.service.UserService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User API", description = "사용자 프로필 API")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(HttpServletRequest request) {
        return ResponseEntity.ok(UserProfileResponse.from(getCurrentUser(request)));
    }

    @PutMapping("/me")
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
