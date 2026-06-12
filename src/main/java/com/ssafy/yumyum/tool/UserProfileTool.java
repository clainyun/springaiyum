package com.ssafy.yumyum.tool;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;

@Component
public class UserProfileTool {

    private static final Logger log = LoggerFactory.getLogger(UserProfileTool.class);

    private final UserRepository userRepository;

    public UserProfileTool(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Tool(description = "사용자 ID로 프로필 정보(닉네임, 목표, 성별, 나이, 신장, 체중, 건강 메모)를 조회합니다.")
    public UserProfile getUserProfile(
            @ToolParam(description = "조회할 사용자의 ID") String userId) {

        log.info("[Tool 호출] UserProfileTool.getUserProfile - userId: {}", userId);

        User user = userRepository.findById(userId);
        if (user == null) {
            log.warn("[Tool 결과] UserProfileTool - 사용자를 찾을 수 없음: {}", userId);
            return null;
        }

        int age = LocalDate.now().getYear() - user.getBirthYear() + 1;
        UserProfile profile = new UserProfile(
                userId,
                user.getNickname(),
                user.getGoal(),
                user.getGender(),
                age,
                user.getHeight(),
                user.getWeight(),
                user.getHealthNote()
        );

        log.info("[Tool 결과] UserProfileTool - 닉네임: {}, 목표: {}, 나이: {}세, 체중: {}kg",
                user.getNickname(), user.getGoal(), age, user.getWeight());

        return profile;
    }

    public record UserProfile(
            String userId,
            String nickname,
            String goal,
            String gender,
            int age,
            double height,
            double weight,
            String healthNote
    ) {}
}
