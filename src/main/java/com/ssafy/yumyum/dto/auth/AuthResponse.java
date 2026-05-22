package com.ssafy.yumyum.dto.auth;

import com.ssafy.yumyum.dto.user.UserSummaryResponse;
import com.ssafy.yumyum.model.User;

public record AuthResponse(String message, UserSummaryResponse user) {

    public static AuthResponse of(String message, User user) {
        return new AuthResponse(message, UserSummaryResponse.from(user));
    }
}
