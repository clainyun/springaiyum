package com.ssafy.yumyum.dto.user;

public record UpdateUserProfileRequest(
        String email,
        String nickname,
        String password,
        String gender,
        Integer birthYear,
        Double height,
        Double weight,
        String goal,
        String healthNote
) {
}
