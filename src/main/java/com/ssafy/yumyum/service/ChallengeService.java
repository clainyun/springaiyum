package com.ssafy.yumyum.service;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.ChallengeParticipant;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.ChallengeRepository;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.util.IdGenerator;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SortUtils;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public ChallengeService(ChallengeRepository challengeRepository, UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
    }

    public List<Challenge> getChallenges() {
        return SortUtils.quickSort(challengeRepository.findAllChallenges(), (a, b) -> a.getEndDate().compareTo(b.getEndDate()));
    }

    public Challenge findChallenge(String challengeId) {
        return challengeRepository.findChallengeById(challengeId);
    }

    public ServiceResult<Challenge> createChallenge(User user, String title, String description, String category, int targetCount, LocalDate endDate) {
        if (title == null || title.trim().isEmpty()) {
            return ServiceResult.failure("챌린지 제목을 입력해 주세요.");
        }
        if (targetCount <= 0) {
            return ServiceResult.failure("목표 횟수는 1 이상이어야 합니다.");
        }
        if (endDate == null || endDate.isBefore(LocalDate.now())) {
            return ServiceResult.failure("종료일을 오늘 이후로 선택해 주세요.");
        }

        Challenge challenge = new Challenge();
        challenge.setId(IdGenerator.next("challenge"));
        challenge.setTitle(title.trim());
        challenge.setDescription(description == null ? "" : description.trim());
        challenge.setCategory(category);
        challenge.setTargetCount(targetCount);
        challenge.setStartDate(LocalDate.now());
        challenge.setEndDate(endDate);
        challenge.setCreatedBy(user.getId());
        challenge.setCreatedAt(LocalDateTime.now());
        challengeRepository.saveChallenge(challenge);
        return ServiceResult.success("챌린지를 생성했습니다.", challenge);
    }

    public ServiceResult<ChallengeMembership> joinChallenge(String challengeId, String userId) {
        if (findMembership(challengeId, userId) != null) {
            return ServiceResult.failure("이미 참여 중인 챌린지입니다.");
        }
        Challenge challenge = challengeRepository.findChallengeById(challengeId);
        if (challenge == null) {
            return ServiceResult.failure("챌린지를 찾을 수 없습니다.");
        }
        ChallengeMembership membership = new ChallengeMembership();
        membership.setId(IdGenerator.next("membership"));
        membership.setChallengeId(challengeId);
        membership.setUserId(userId);
        membership.setProgress(0);
        membership.setStatus("joined");
        membership.setJoinedAt(LocalDateTime.now());
        membership.setUpdatedAt(LocalDateTime.now());
        challengeRepository.saveMembership(membership);
        return ServiceResult.success("챌린지에 참여했습니다.", membership);
    }

    public void leaveChallenge(String challengeId, String userId) {
        ChallengeMembership membership = findMembership(challengeId, userId);
        if (membership != null) {
            challengeRepository.deleteMembership(membership.getId());
        }
    }

    public ServiceResult<ChallengeMembership> updateProgress(String challengeId, String userId, int progress) {
        ChallengeMembership membership = findMembership(challengeId, userId);
        Challenge challenge = challengeRepository.findChallengeById(challengeId);
        if (membership == null || challenge == null) {
            return ServiceResult.failure("진행률을 수정할 대상이 없습니다.");
        }
        membership.setProgress(Math.max(0, Math.min(progress, challenge.getTargetCount())));
        membership.setStatus(membership.getProgress() >= challenge.getTargetCount() ? "completed" : "joined");
        membership.setUpdatedAt(LocalDateTime.now());
        challengeRepository.saveMembership(membership);
        return ServiceResult.success("진행률을 업데이트했습니다.", membership);
    }

    public void deleteChallenge(String challengeId, User user) {
        Challenge challenge = challengeRepository.findChallengeById(challengeId);
        if (challenge != null && user.getId().equals(challenge.getCreatedBy())) {
            challengeRepository.deleteChallenge(challengeId);
        }
    }

    public Map<String, ChallengeMembership> membershipMap(String userId) {
        Map<String, ChallengeMembership> result = new HashMap<>();
        for (ChallengeMembership membership : challengeRepository.findAllMemberships()) {
            if (userId.equals(membership.getUserId())) {
                result.put(membership.getChallengeId(), membership);
            }
        }
        return result;
    }

    public ChallengeMembership findMembership(String challengeId, String userId) {
        for (ChallengeMembership membership : challengeRepository.findAllMemberships()) {
            if (challengeId.equals(membership.getChallengeId()) && userId.equals(membership.getUserId())) {
                return membership;
            }
        }
        return null;
    }

    public List<ChallengeMembership> membershipsForUser(String userId) {
        List<ChallengeMembership> result = new ArrayList<>();
        for (ChallengeMembership membership : challengeRepository.findAllMemberships()) {
            if (userId.equals(membership.getUserId())) {
                result.add(membership);
            }
        }
        return result;
    }

    public int countJoined(String userId) {
        return membershipsForUser(userId).size();
    }

    public int countCompleted(String userId) {
        int completed = 0;
        for (ChallengeMembership membership : membershipsForUser(userId)) {
            if ("completed".equals(membership.getStatus())) {
                completed++;
            }
        }
        return completed;
    }

    public List<ChallengeParticipant> participants(String challengeId) {
        List<ChallengeParticipant> result = new ArrayList<>();
        for (ChallengeMembership membership : challengeRepository.findAllMemberships()) {
            if (!challengeId.equals(membership.getChallengeId())) {
                continue;
            }
            User user = userRepository.findById(membership.getUserId());
            ChallengeParticipant participant = new ChallengeParticipant();
            participant.setUserId(membership.getUserId());
            participant.setNickname(user == null ? "알 수 없음" : user.getNickname());
            participant.setProgress(membership.getProgress());
            result.add(participant);
        }
        return SortUtils.quickSort(result, (a, b) -> Integer.compare(b.getProgress(), a.getProgress()));
    }

    public Map<String, List<ChallengeParticipant>> participantMap(List<Challenge> challenges) {
        Map<String, List<ChallengeParticipant>> result = new HashMap<>();
        for (Challenge challenge : challenges) {
            result.put(challenge.getId(), participants(challenge.getId()));
        }
        return result;
    }
}
