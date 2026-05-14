package com.ssafy.yumyum.service;

import com.ssafy.yumyum.model.FollowRelation;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.SocialRepository;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.util.IdGenerator;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SortUtils;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SocialService {

    private final SocialRepository socialRepository;
    private final UserRepository userRepository;

    public SocialService(SocialRepository socialRepository, UserRepository userRepository) {
        this.socialRepository = socialRepository;
        this.userRepository = userRepository;
    }

    public List<User> getFollowing(String userId) {
        List<User> result = new ArrayList<>();
        for (FollowRelation relation : socialRepository.findAll()) {
            if (userId.equals(relation.getFollowerId())) {
                User user = userRepository.findById(relation.getFolloweeId());
                if (user != null && user.isActive()) {
                    result.add(user);
                }
            }
        }
        return SortUtils.quickSort(result, (a, b) -> a.getNickname().compareToIgnoreCase(b.getNickname()));
    }

    public List<User> getFollowers(String userId) {
        List<User> result = new ArrayList<>();
        for (FollowRelation relation : socialRepository.findAll()) {
            if (userId.equals(relation.getFolloweeId())) {
                User user = userRepository.findById(relation.getFollowerId());
                if (user != null && user.isActive()) {
                    result.add(user);
                }
            }
        }
        return SortUtils.quickSort(result, (a, b) -> a.getNickname().compareToIgnoreCase(b.getNickname()));
    }

    public boolean isFollowing(String userId, String targetUserId) {
        for (FollowRelation relation : socialRepository.findAll()) {
            if (userId.equals(relation.getFollowerId()) && targetUserId.equals(relation.getFolloweeId())) {
                return true;
            }
        }
        return false;
    }

    public ServiceResult<Void> follow(String userId, String targetUserId) {
        if (userId.equals(targetUserId)) {
            return ServiceResult.failure("자기 자신은 팔로우할 수 없습니다.");
        }
        if (isFollowing(userId, targetUserId)) {
            return ServiceResult.failure("이미 팔로우 중입니다.");
        }
        FollowRelation relation = new FollowRelation();
        relation.setId(IdGenerator.next("follow"));
        relation.setFollowerId(userId);
        relation.setFolloweeId(targetUserId);
        relation.setCreatedAt(LocalDateTime.now());
        socialRepository.save(relation);
        return ServiceResult.success("팔로우를 추가했습니다.", null);
    }

    public void unfollow(String userId, String targetUserId) {
        for (FollowRelation relation : socialRepository.findAll()) {
            if (userId.equals(relation.getFollowerId()) && targetUserId.equals(relation.getFolloweeId())) {
                socialRepository.delete(relation.getId());
                break;
            }
        }
    }

    public List<User> getSuggestions(String userId, int limit) {
        List<User> result = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (!user.isActive() || userId.equals(user.getId()) || isFollowing(userId, user.getId())) {
                continue;
            }
            result.add(user);
        }
        List<User> sorted = SortUtils.quickSort(result, (a, b) -> Integer.compare(countFollowers(b.getId()), countFollowers(a.getId())));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public List<User> getLeaderboard(String userId, int limit) {
        List<User> candidates = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (user.isActive() && !userId.equals(user.getId())) {
                candidates.add(user);
            }
        }
        List<User> sorted = SortUtils.quickSort(candidates, (a, b) -> Integer.compare(countFollowers(b.getId()), countFollowers(a.getId())));
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public int countFollowers(String userId) {
        int count = 0;
        for (FollowRelation relation : socialRepository.findAll()) {
            if (userId.equals(relation.getFolloweeId())) {
                count++;
            }
        }
        return count;
    }

    public int countFollowing(String userId) {
        int count = 0;
        for (FollowRelation relation : socialRepository.findAll()) {
            if (userId.equals(relation.getFollowerId())) {
                count++;
            }
        }
        return count;
    }

    public Map<String, Integer> followerCountMap(List<User> users) {
        Map<String, Integer> result = new HashMap<>();
        for (User user : users) {
            result.put(user.getId(), countFollowers(user.getId()));
        }
        return result;
    }
}
