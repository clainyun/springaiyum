package com.ssafy.yumyum.service;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.CommunityComment;
import com.ssafy.yumyum.model.CommunityPost;
import com.ssafy.yumyum.model.FollowRelation;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.ChallengeRepository;
import com.ssafy.yumyum.repository.CommunityRepository;
import com.ssafy.yumyum.repository.MealRepository;
import com.ssafy.yumyum.repository.SocialRepository;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.util.ServiceResult;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final SocialRepository socialRepository;
    private final ChallengeRepository challengeRepository;
    private final CommunityRepository communityRepository;

    public UserService(UserRepository userRepository,
                       MealRepository mealRepository,
                       SocialRepository socialRepository,
                       ChallengeRepository challengeRepository,
                       CommunityRepository communityRepository) {
        this.userRepository = userRepository;
        this.mealRepository = mealRepository;
        this.socialRepository = socialRepository;
        this.challengeRepository = challengeRepository;
        this.communityRepository = communityRepository;
    }

    public User findById(String userId) {
        return userId == null ? null : userRepository.findById(userId);
    }

    public List<User> findAllActiveUsers() {
        List<User> users = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            if (user.isActive()) {
                users.add(user);
            }
        }

        return users;
    }

    public ServiceResult<User> updateProfile(User user, String email, String nickname, String password, String gender,
                                             int birthYear, double height, double weight, String goal, String healthNote) {
        if (email == null || !email.contains("@")) {
            return ServiceResult.failure("올바른 이메일을 입력해 주세요.");
        }

        String trimmedEmail = email.trim();

        User duplicated = userRepository.findByEmail(trimmedEmail);
        if (duplicated != null && !duplicated.getId().equals(user.getId())) {
            return ServiceResult.failure("이미 사용 중인 이메일입니다.");
        }

        if (nickname == null || nickname.trim().isEmpty()) {
            return ServiceResult.failure("닉네임을 입력해 주세요.");
        }

        user.setEmail(trimmedEmail);
        user.setNickname(nickname.trim());

        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 8) {
                return ServiceResult.failure("비밀번호는 8자 이상이어야 합니다.");
            }
            user.setPassword(password);
        }

        user.setGender(gender);
        user.setBirthYear(birthYear);
        user.setHeight(height);
        user.setWeight(weight);
        user.setGoal(goal);
        user.setHealthNote(healthNote == null ? "" : healthNote.trim());

        userRepository.save(user);

        return ServiceResult.success("프로필을 수정했습니다.", user);
    }

    public void deactivate(User user) {
        user.setActive(false);
        userRepository.save(user);
    }

    public void delete(User user) {
        deleteMeals(user.getId());
        deleteFollowRelations(user.getId());
        deleteChallenges(user.getId());
        deleteCommunityData(user.getId());

        userRepository.delete(user.getId());
    }

    private void deleteMeals(String userId) {
        for (Meal meal : mealRepository.findAll()) {
            if (userId.equals(meal.getUserId())) {
                mealRepository.delete(meal.getId());
            }
        }
    }

    private void deleteFollowRelations(String userId) {
        for (FollowRelation relation : socialRepository.findAll()) {
            if (userId.equals(relation.getFollowerId()) || userId.equals(relation.getFolloweeId())) {
                socialRepository.delete(relation.getId());
            }
        }
    }

    private void deleteChallenges(String userId) {
        for (Challenge challenge : challengeRepository.findAllChallenges()) {
            if (userId.equals(challenge.getCreatedBy())) {
                challengeRepository.deleteChallenge(challenge.getId());
            }
        }

        for (ChallengeMembership membership : challengeRepository.findAllMemberships()) {
            if (userId.equals(membership.getUserId())) {
                challengeRepository.deleteMembership(membership.getId());
            }
        }
    }

    private void deleteCommunityData(String userId) {
        for (CommunityPost post : communityRepository.findAllPosts()) {
            if (userId.equals(post.getUserId())) {
                communityRepository.deletePost(post.getId());
            }
        }

        for (CommunityComment comment : communityRepository.findAllComments()) {
            if (userId.equals(comment.getUserId())) {
                communityRepository.deleteComment(comment.getId());
            }
        }
    }
}