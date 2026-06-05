package com.ssafy.yumyum.dto.user;

import com.ssafy.yumyum.dto.meal.DailyGoalResponse;
import com.ssafy.yumyum.model.DailyGoal;
import com.ssafy.yumyum.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileDashboardResponse(
        @Schema(description = "사용자 프로필")
        UserProfileResponse user,
        @Schema(description = "일일 목표")
        DailyGoalResponse dailyGoal,
        @Schema(description = "식단 기록 수", example = "12")
        int mealCount,
        @Schema(description = "팔로잉 수", example = "3")
        int followingCount,
        @Schema(description = "팔로워 수", example = "5")
        int followerCount,
        @Schema(description = "참여 챌린지 수", example = "2")
        int joinedChallengeCount
) {
    public static UserProfileDashboardResponse from(User user,
                                                    DailyGoal dailyGoal,
                                                    int mealCount,
                                                    int followingCount,
                                                    int followerCount,
                                                    int joinedChallengeCount) {
        return new UserProfileDashboardResponse(
                UserProfileResponse.from(user),
                DailyGoalResponse.from(dailyGoal),
                mealCount,
                followingCount,
                followerCount,
                joinedChallengeCount
        );
    }
}
