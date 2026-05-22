package com.ssafy.yumyum.dto.auth;

public record SignupRequest(
        String email,
        String password,
        String nickname,
        String gender,
        Integer birthYear,
        Double height,
        Double weight,
        String goal,
        String healthNote
) {
}
