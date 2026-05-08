package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.Challenge;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.BaseController;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/challenges")
public class ChallengeController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }
        List<Challenge> challenges = AppContainer.getChallengeService().getChallenges();
        req.setAttribute("pageTitle", "챌린지");
        req.setAttribute("activeNav", "challenge");
        req.setAttribute("challenges", challenges);
        req.setAttribute("membershipMap", AppContainer.getChallengeService().membershipMap(user.getId()));
        req.setAttribute("participantMap", AppContainer.getChallengeService().participantMap(challenges));
        req.setAttribute("joinedCount", AppContainer.getChallengeService().countJoined(user.getId()));
        req.setAttribute("completedCount", AppContainer.getChallengeService().countCompleted(user.getId()));
        req.setAttribute("createdCount", createdCount(challenges, user.getId()));
        render(req, resp, "challenge/index");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }
        String action = req.getParameter("action");
        if ("create".equals(action)) {
            ServiceResult<?> result = AppContainer.getChallengeService().createChallenge(
                user,
                req.getParameter("title"),
                req.getParameter("description"),
                req.getParameter("category"),
                parseInt(req.getParameter("targetCount"), 7),
                parseDate(req.getParameter("endDate"))
            );
            SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        } else if ("join".equals(action)) {
            ServiceResult<?> result = AppContainer.getChallengeService().joinChallenge(req.getParameter("challengeId"), user.getId());
            SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        } else if ("leave".equals(action)) {
            AppContainer.getChallengeService().leaveChallenge(req.getParameter("challengeId"), user.getId());
            SessionUtils.flash(req.getSession(), "info", "챌린지에서 나갔습니다.");
        } else if ("progress".equals(action)) {
            ServiceResult<?> result = AppContainer.getChallengeService().updateProgress(
                req.getParameter("challengeId"),
                user.getId(),
                parseInt(req.getParameter("progress"), 0)
            );
            SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        } else if ("delete".equals(action)) {
            AppContainer.getChallengeService().deleteChallenge(req.getParameter("challengeId"), user);
            SessionUtils.flash(req.getSession(), "success", "챌린지를 삭제했습니다.");
        }
        redirect(req, resp, "/challenges");
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
}
