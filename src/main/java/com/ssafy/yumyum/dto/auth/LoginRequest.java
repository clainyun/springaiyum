package com.ssafy.yumyum.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
        @Schema(description = "로그인 이메일", example = "demo@yamyam.com")
        String email,
        @Schema(description = "로그인 비밀번호", example = "Demo1234!")
        String password
) {
}
