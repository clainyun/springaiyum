package com.ssafy.yumyum.dto.user;

import com.ssafy.yumyum.model.User;

public record UserSummaryResponse(
        String id,
        String email,
        String nickname,
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
