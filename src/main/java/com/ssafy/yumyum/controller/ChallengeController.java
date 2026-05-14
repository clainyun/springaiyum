package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.ChallengeMembership;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;
import com.ssafy.yumyum.util.ViewHelper;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/challenges")
public class ChallengeController {

    private static final String CHALLENGE_INDEX_VIEW = "challenge/index";
    private static final String CHALLENGES_REDIRECT = "redirect:/challenges";

    @ModelAttribute
    public void exposeFlash(HttpServletRequest req) {
        SessionUtils.exposeFlash(req);
    }

    @ModelAttribute("pageTitle")
    public String pageTitle() {
        return "챌린지";
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "challenge";
    }

    @ModelAttribute("challengeForm")
    public ChallengeForm challengeForm() {
        ChallengeForm form = new ChallengeForm();
        form.setCategory("습관");
        form.setTargetCount("7");
        return form;
    }

    @GetMapping
    public String getChallenges(@RequestAttribute("currentUser") User user, Model model) {
        List<Challenge> challenges = AppContainer.getChallengeService().getChallenges();
        Map<String, ChallengeMembership> membershipMap = AppContainer.getChallengeService().membershipMap(user.getId());

        model.addAttribute("challenges", challenges);
        model.addAttribute("membershipMap", membershipMap);
        model.addAttribute("participantMap", AppContainer.getChallengeService().participantMap(challenges));
        model.addAttribute("joinedCount", AppContainer.getChallengeService().countJoined(user.getId()));
        model.addAttribute("completedCount", AppContainer.getChallengeService().countCompleted(user.getId()));
        model.addAttribute("createdCount", createdCount(challenges, user.getId()));
        model.addAttribute("challengeCount", challenges.size());
        model.addAttribute("periodLabelMap", periodLabelMap(challenges));
        model.addAttribute("statusLabelMap", statusLabelMap(challenges, membershipMap));
        model.addAttribute("ownedChallengeMap", ownedChallengeMap(challenges, user.getId()));
        return CHALLENGE_INDEX_VIEW;
    }

    @PostMapping
    public String createChallenge(
        @RequestAttribute("currentUser") User user,
        @ModelAttribute("challengeForm") ChallengeForm form,
        HttpServletRequest req
    ) {
        ServiceResult<?> result = AppContainer.getChallengeService().createChallenge(
            user,
            form.getTitle(),
            form.getDescription(),
            form.getCategory(),
            parseInt(form.getTargetCount(), 7),
            parseDate(form.getEndDate())
        );
        SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        return CHALLENGES_REDIRECT;
    }

    @PostMapping("/{challengeId}/memberships")
    public String joinChallenge(
        @RequestAttribute("currentUser") User user,
        @PathVariable String challengeId,
        HttpServletRequest req
    ) {
        ServiceResult<?> result = AppContainer.getChallengeService().joinChallenge(challengeId, user.getId());
        SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        return CHALLENGES_REDIRECT;
    }

    @PatchMapping("/{challengeId}/memberships/me")
    public String updateProgress(
        @RequestAttribute("currentUser") User user,
        @PathVariable String challengeId,
        @RequestParam(required = false) String progress,
        HttpServletRequest req
    ) {
        ServiceResult<?> result = AppContainer.getChallengeService().updateProgress(
            challengeId,
            user.getId(),
            parseInt(progress, 0)
        );
        SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        return CHALLENGES_REDIRECT;
    }

    @DeleteMapping("/{challengeId}/memberships/me")
    public String leaveChallenge(
        @RequestAttribute("currentUser") User user,
        @PathVariable String challengeId,
        HttpServletRequest req
    ) {
        AppContainer.getChallengeService().leaveChallenge(challengeId, user.getId());
        SessionUtils.flash(req.getSession(), "info", "챌린지에서 나갔습니다.");
        return CHALLENGES_REDIRECT;
    }

    @DeleteMapping("/{challengeId}")
    public String deleteChallenge(
        @RequestAttribute("currentUser") User user,
        @PathVariable String challengeId,
        HttpServletRequest req
    ) {
        AppContainer.getChallengeService().deleteChallenge(challengeId, user);
        SessionUtils.flash(req.getSession(), "success", "챌린지를 삭제했습니다.");
        return CHALLENGES_REDIRECT;
    }

    private Map<String, String> periodLabelMap(List<Challenge> challenges) {
        Map<String, String> result = new HashMap<>();
        for (Challenge challenge : challenges) {
            result.put(
                challenge.getId(),
                ViewHelper.formatDate(challenge.getStartDate()) + " ~ " + ViewHelper.formatDate(challenge.getEndDate())
            );
        }
        return result;
    }

    private Map<String, String> statusLabelMap(List<Challenge> challenges, Map<String, ChallengeMembership> membershipMap) {
        Map<String, String> result = new HashMap<>();
        for (Challenge challenge : challenges) {
            ChallengeMembership membership = membershipMap.get(challenge.getId());
            result.put(
                challenge.getId(),
                membership == null ? "모집 중" : ViewHelper.challengeStatusLabel(membership.getStatus())
            );
        }
        return result;
    }

    private Map<String, Boolean> ownedChallengeMap(List<Challenge> challenges, String userId) {
        Map<String, Boolean> result = new HashMap<>();
        for (Challenge challenge : challenges) {
            result.put(challenge.getId(), userId.equals(challenge.getCreatedBy()));
        }
        return result;
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

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private LocalDate parseDate(String raw) {
        try {
            return raw == null || raw.trim().isEmpty() ? null : LocalDate.parse(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Data
    public static class ChallengeForm {
        private String title;
        private String description;
        private String category;
        private String targetCount;
        private String endDate;
    }
}
