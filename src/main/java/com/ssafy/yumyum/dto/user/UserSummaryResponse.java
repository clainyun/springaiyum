package com.ssafy.yumyum.dto.user;

import com.ssafy.yumyum.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserSummaryResponse(
        @Schema(description = "사용자 ID", example = "user_demo")
        String id,
        @Schema(description = "이메일", example = "demo@yamyam.com")
        String email,
        @Schema(description = "닉네임", example = "데모 사용자")
        String nickname,
        @Schema(description = "활성 여부", example = "true")
        boolean active
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.isActive()
        );
    }
}
