package com.ssafy.yumyum.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateUserProfileRequest(
        @Schema(description = "이메일", example = "demo@yamyam.com")
        String email,
        @Schema(description = "닉네임", example = "데모 사용자")
        String nickname,
        @Schema(description = "변경할 비밀번호", example = "NewDemo1234!")
        String password,
        @Schema(description = "성별", example = "male")
        String gender,
        @Schema(description = "출생 연도", example = "1996")
        Integer birthYear,
        @Schema(description = "키(cm)", example = "176")
        Double height,
        @Schema(description = "몸무게(kg)", example = "72")
        Double weight,
        @Schema(description = "목표", example = "health")
        String goal,
        @Schema(description = "건강 메모", example = "짠 음식 주의")
        String healthNote
) {
}
