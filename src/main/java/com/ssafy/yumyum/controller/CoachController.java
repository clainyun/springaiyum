package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/coach")
public class CoachController {

    private static final String LOGIN_REDIRECT = "redirect:/auth/login";
    private static final String COACH_INDEX_VIEW = "coach/index";

    @ModelAttribute
    public void exposeFlash(HttpServletRequest req) {
        SessionUtils.exposeFlash(req);
    }

    @ModelAttribute("pageTitle")
    public String pageTitle() {
        return "AI 코치";
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "coach";
    }

    @ModelAttribute("currentUser")
    public User currentUser(
        @SessionAttribute(value = "loginUserId", required = false) String loginUserId,
        HttpServletRequest req
    ) {
        User user = AppContainer.getUserService().findById(loginUserId);
        if (user == null || !user.isActive()) {
            SessionUtils.flash(req.getSession(), "warning", "로그인이 필요한 메뉴입니다.");
            return null;
        }
        return user;
    }

    @GetMapping
    public String getCoach(@ModelAttribute("currentUser") User user) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }
        return COACH_INDEX_VIEW;
    }
}
