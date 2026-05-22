package com.ssafy.yumyum.dto.user;

import com.ssafy.yumyum.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileResponse(
        @Schema(description = "사용자 ID", example = "user_demo")
        String id,
        @Schema(description = "이메일", example = "demo@yamyam.com")
        String email,
        @Schema(description = "닉네임", example = "데모 사용자")
        String nickname,
        @Schema(description = "성별", example = "male")
        String gender,
        @Schema(description = "출생 연도", example = "1996")
        int birthYear,
        @Schema(description = "키(cm)", example = "176")
        double height,
        @Schema(description = "몸무게(kg)", example = "72")
        double weight,
        @Schema(description = "목표", example = "health")
        String goal,
        @Schema(description = "건강 메모", example = "짠 음식 주의")
        String healthNote,
        @Schema(description = "활성 여부", example = "true")
        boolean active
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getGender(),
                user.getBirthYear(),
                user.getHeight(),
                user.getWeight(),
                user.getGoal(),
                user.getHealthNote(),
                user.isActive()
        );
    }
}
