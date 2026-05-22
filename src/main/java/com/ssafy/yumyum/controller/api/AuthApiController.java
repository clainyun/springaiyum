package com.ssafy.yumyum.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.auth.AuthResponse;
import com.ssafy.yumyum.dto.auth.LoginRequest;
import com.ssafy.yumyum.dto.auth.SignupRequest;
import com.ssafy.yumyum.dto.common.MessageResponse;
import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.service.AuthService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth API", description = "세션 기반 인증 API")
public class AuthApiController {

    private final AuthService authService;

    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        ServiceResult<User> result = authService.login(request.email(), request.password());
        if (!result.isOk()) {
            throw new CustomException(HttpStatus.UNAUTHORIZED.value(), result.getMessage());
        }

        SessionUtils.login(httpRequest.getSession(), result.getData().getId());
        return ResponseEntity.ok(AuthResponse.of(result.getMessage(), result.getData()));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request, HttpServletRequest httpRequest) {
        ServiceResult<User> result = authService.register(
                request.email(),
                request.password(),
                request.nickname(),
                request.gender(),
                request.birthYear() == null ? 1998 : request.birthYear(),
                request.height() == null ? 165.0 : request.height(),
                request.weight() == null ? 60.0 : request.weight(),
                request.goal(),
                request.healthNote()
        );
        if (!result.isOk()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }

        SessionUtils.login(httpRequest.getSession(), result.getData().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.of(result.getMessage(), result.getData()));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        SessionUtils.logout(request.getSession(false));
        return ResponseEntity.ok(new MessageResponse("로그아웃되었습니다."));
    }
}
