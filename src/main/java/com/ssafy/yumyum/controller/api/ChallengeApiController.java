package com.ssafy.yumyum.controller.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.common.MessageResponse;
import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.ChallengeParticipant;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.ChallengeService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;
import com.ssafy.yumyum.util.ViewHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/challenges")
@Tag(name = "Challenge API", description = "챌린지 API")
public class ChallengeApiController {

    private final ChallengeService challengeService;
    private final UserRepository userRepository;

    public ChallengeApiController(ChallengeService challengeService, UserRepository userRepository) {
        this.challengeService = challengeService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "챌린지 목록 조회")
    public ResponseEntity<ChallengeBoardResponse> list(HttpServletRequest request) {
        User user = getCurrentUser(request);
        List<Challenge> challenges = challengeService.getChallenges();
        Map<String, ChallengeMembership> membershipMap = challengeService.membershipMap(user.getId());
        return ResponseEntity.ok(new ChallengeBoardResponse(
                challengeService.countJoined(user.getId()),
                challengeService.countCompleted(user.getId()),
                createdCount(challenges, user.getId()),
                challenges.size(),
                challenges.stream()
                        .map(challenge -> challengeResponse(challenge, membershipMap.get(challenge.getId()), user))
                        .toList()
        ));
    }

    @PostMapping
    @Operation(summary = "챌린지 생성")
    public ResponseEntity<MessageResponse> create(@RequestBody ChallengeRequest request,
                                                  HttpServletRequest httpRequest) {
        ServiceResult<?> result = challengeService.createChallenge(
                getCurrentUser(httpRequest),
                request.title(),
                request.description(),
                request.category(),
                request.targetCount(),
                parseDate(request.endDate())
        );
        if (!result.isOk()) {
            throw new CustomException(400, result.getMessage());
        }
        return ResponseEntity.status(201).body(new MessageResponse(result.getMessage()));
    }

    @PostMapping("/{challengeId}/memberships")
    @Operation(summary = "챌린지 참여")
    public ResponseEntity<MessageResponse> join(@PathVariable String challengeId, HttpServletRequest request) {
        ServiceResult<?> result = challengeService.joinChallenge(challengeId, getCurrentUser(request).getId());
        if (!result.isOk()) {
            throw new CustomException(400, result.getMessage());
        }
        return ResponseEntity.ok(new MessageResponse(result.getMessage()));
    }

    @PatchMapping("/{challengeId}/memberships/me")
    @Operation(summary = "내 진행률 수정")
    public ResponseEntity<MessageResponse> updateProgress(@PathVariable String challengeId,
                                                          @RequestBody ProgressRequest request,
                                                          HttpServletRequest httpRequest) {
        ServiceResult<?> result = challengeService.updateProgress(
                challengeId,
                getCurrentUser(httpRequest).getId(),
                request.progress()
        );
        if (!result.isOk()) {
            throw new CustomException(400, result.getMessage());
        }
        return ResponseEntity.ok(new MessageResponse(result.getMessage()));
    }

    @DeleteMapping("/{challengeId}/memberships/me")
    @Operation(summary = "챌린지 탈퇴")
    public ResponseEntity<MessageResponse> leave(@PathVariable String challengeId, HttpServletRequest request) {
        challengeService.leaveChallenge(challengeId, getCurrentUser(request).getId());
        return ResponseEntity.ok(new MessageResponse("챌린지에서 나갔습니다."));
    }

    @DeleteMapping("/{challengeId}")
    @Operation(summary = "챌린지 삭제")
    public ResponseEntity<MessageResponse> delete(@PathVariable String challengeId, HttpServletRequest request) {
        challengeService.deleteChallenge(challengeId, getCurrentUser(request));
        return ResponseEntity.ok(new MessageResponse("챌린지를 삭제했습니다."));
    }

    private ChallengeResponse challengeResponse(Challenge challenge, ChallengeMembership membership, User user) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getCategory(),
                challenge.getTargetCount(),
                ViewHelper.formatDate(challenge.getStartDate()) + " ~ " + ViewHelper.formatDate(challenge.getEndDate()),
                user.getId().equals(challenge.getCreatedBy()),
                membership == null ? null : MembershipResponse.from(membership),
                membership == null ? "모집 중" : ViewHelper.challengeStatusLabel(membership.getStatus()),
                challengeService.participants(challenge.getId()).stream().map(ParticipantResponse::from).toList()
        );
    }

    private int createdCount(List<Challenge> challenges, String userId) {
        int count = 0;
        for (Challenge challenge : challenges) {
            if (userId.equals(challenge.getCreatedBy())) {
                count++;
            }
        }
        return count;
    }

    private LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        String loginUserId = SessionUtils.currentUserId(request);
        if (loginUserId == null) {
            throw new CustomException(401, "로그인이 필요합니다.");
        }
        User user = userRepository.findById(loginUserId);
        if (user == null || !user.isActive()) {
            throw new CustomException(401, "로그인 정보를 찾을 수 없습니다.");
        }
        return user;
    }

    public record ChallengeBoardResponse(int joinedCount, int completedCount, int createdCount, int challengeCount,
                                         List<ChallengeResponse> challenges) {
    }

    public record ChallengeResponse(String id, String title, String description, String category, int targetCount,
                                    String periodLabel, boolean owned, MembershipResponse membership,
                                    String statusLabel, List<ParticipantResponse> participants) {
    }

    public record MembershipResponse(String id, int progress, String status) {
        public static MembershipResponse from(ChallengeMembership membership) {
            return new MembershipResponse(membership.getId(), membership.getProgress(), membership.getStatus());
        }
    }

    public record ParticipantResponse(String userId, String nickname, int progress) {
        public static ParticipantResponse from(ChallengeParticipant participant) {
            return new ParticipantResponse(participant.getUserId(), participant.getNickname(), participant.getProgress());
        }
    }

    public record ChallengeRequest(String title, String description, String category, int targetCount, String endDate) {
    }

    public record ProgressRequest(int progress) {
    }
}
