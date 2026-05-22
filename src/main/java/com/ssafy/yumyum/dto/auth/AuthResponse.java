package com.ssafy.yumyum.dto.auth;

import com.ssafy.yumyum.dto.user.UserSummaryResponse;
import com.ssafy.yumyum.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "처리 결과 메시지", example = "로그인되었습니다.")
        String message,
        @Schema(description = "로그인 또는 가입된 사용자 요약 정보")
        UserSummaryResponse user
) {

    public static AuthResponse of(String message, User user) {
        return new AuthResponse(message, UserSummaryResponse.from(user));
    }
}
