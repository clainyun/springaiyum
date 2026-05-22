package com.ssafy.yumyum.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignupRequest(
        @Schema(description = "회원 이메일", example = "newuser@yamyam.com")
        String email,
        @Schema(description = "회원 비밀번호", example = "Demo1234!")
        String password,
        @Schema(description = "닉네임", example = "새사용자")
        String nickname,
        @Schema(description = "성별", example = "female")
        String gender,
        @Schema(description = "출생 연도", example = "1998")
        Integer birthYear,
        @Schema(description = "키(cm)", example = "165")
        Double height,
        @Schema(description = "몸무게(kg)", example = "60")
        Double weight,
        @Schema(description = "목표", example = "health")
        String goal,
        @Schema(description = "건강 메모", example = "짠 음식 주의")
        String healthNote
) {
}
