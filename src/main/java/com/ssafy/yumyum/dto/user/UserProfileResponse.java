package com.ssafy.yumyum.dto.user;

import com.ssafy.yumyum.model.User;

public record UserProfileResponse(
        String id,
        String email,
        String nickname,
        String gender,
        int birthYear,
        double height,
        double weight,
        String goal,
        String healthNote,
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
