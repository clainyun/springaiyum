package com.ssafy.yumyum.controller;

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

@WebServlet(urlPatterns = {"/auth/login", "/auth/signup", "/auth/logout"})
public class AuthController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/auth/logout".equals(servletPath)) {
            SessionUtils.logout(req.getSession(false));
            req.getSession(true);
            SessionUtils.flash(req.getSession(), "success", "로그아웃되었습니다.");
            redirect(req, resp, "/auth/login");
            return;
        }
        if (SessionUtils.currentUserId(req) != null) {
            redirect(req, resp, "/home");
            return;
        }
        req.setAttribute("pageTitle", "/auth/login".equals(servletPath) ? "로그인" : "회원가입");
        req.setAttribute("activeNav", servletPath.endsWith("login") ? "login" : "signup");
        render(req, resp, "/auth/login".equals(servletPath) ? "auth/login" : "auth/signup");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/auth/login".equals(servletPath)) {
            handleLogin(req, resp);
            return;
        }
        handleSignup(req, resp);
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServiceResult<User> result = AppContainer.getAuthService().login(req.getParameter("email"), req.getParameter("password"));
        if (!result.isOk()) {
            req.setAttribute("pageTitle", "로그인");
            req.setAttribute("activeNav", "login");
            req.setAttribute("errorMessage", result.getMessage());
            req.setAttribute("email", req.getParameter("email"));
            render(req, resp, "auth/login");
            return;
        }
        SessionUtils.login(req.getSession(), result.getData().getId());
        SessionUtils.flash(req.getSession(), "success", "환영합니다, " + result.getData().getNickname() + "님.");
        redirect(req, resp, "/home");
    }

    private void handleSignup(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int birthYear = parseInt(req.getParameter("birthYear"), 1998);
        double height = parseDouble(req.getParameter("height"), 165);
        double weight = parseDouble(req.getParameter("weight"), 60);

        ServiceResult<User> result = AppContainer.getAuthService().register(
            req.getParameter("email"),
            req.getParameter("password"),
            req.getParameter("nickname"),
            req.getParameter("gender"),
            birthYear,
            height,
            weight,
            req.getParameter("goal"),
            req.getParameter("healthNote")
        );

        if (!result.isOk()) {
            req.setAttribute("pageTitle", "회원가입");
            req.setAttribute("activeNav", "signup");
            req.setAttribute("errorMessage", result.getMessage());
            req.setAttribute("form", req.getParameterMap());
            render(req, resp, "auth/signup");
            return;
        }

        SessionUtils.login(req.getSession(), result.getData().getId());
        SessionUtils.flash(req.getSession(), "success", "회원가입이 완료되었습니다.");
        redirect(req, resp, "/home");
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private double parseDouble(String raw, double fallback) {
        try {
            return Double.parseDouble(raw);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
