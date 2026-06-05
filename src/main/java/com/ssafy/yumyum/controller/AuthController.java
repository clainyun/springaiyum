package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.service.AuthService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/legacy/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request, Model model) {
        if (SessionUtils.currentUserId(request) != null) {
            return "redirect:/legacy/home";
        }

        model.addAttribute("pageTitle", "로그인");
        model.addAttribute("activeNav", "login");

        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {

        ServiceResult<User> result = authService.login(email, password);

        if (!result.isOk()) {
            model.addAttribute("pageTitle", "로그인");
            model.addAttribute("activeNav", "login");
            model.addAttribute("errorMessage", result.getMessage());
            model.addAttribute("email", email);

            return "auth/login";
        }

        SessionUtils.login(request.getSession(), result.getData().getId());
        SessionUtils.flash(
                request.getSession(),
                "success",
                "환영합니다, " + result.getData().getNickname() + "님."
        );

        return "redirect:/legacy/home";
    }

    @GetMapping("/signup")
    public String signupForm(HttpServletRequest request, Model model) {
        if (SessionUtils.currentUserId(request) != null) {
            return "redirect:/legacy/home";
        }

        model.addAttribute("pageTitle", "회원가입");
        model.addAttribute("activeNav", "signup");

        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(HttpServletRequest request, Model model) {
        int birthYear = parseInt(request.getParameter("birthYear"), 1998);
        double height = parseDouble(request.getParameter("height"), 165);
        double weight = parseDouble(request.getParameter("weight"), 60);

        ServiceResult<User> result = authService.register(
                request.getParameter("email"),
                request.getParameter("password"),
                request.getParameter("nickname"),
                request.getParameter("gender"),
                birthYear,
                height,
                weight,
                request.getParameter("goal"),
                request.getParameter("healthNote")
        );

        if (!result.isOk()) {
            model.addAttribute("pageTitle", "회원가입");
            model.addAttribute("activeNav", "signup");
            model.addAttribute("errorMessage", result.getMessage());
            model.addAttribute("form", request.getParameterMap());

            return "auth/signup";
        }

        SessionUtils.login(request.getSession(), result.getData().getId());
        SessionUtils.flash(request.getSession(), "success", "회원가입이 완료되었습니다.");

        return "redirect:/legacy/home";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        SessionUtils.logout(request.getSession(false));

        HttpSession session = request.getSession(true);
        SessionUtils.flash(session, "success", "로그아웃되었습니다.");

        return "redirect:/legacy/auth/login";
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
