package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(HttpServletRequest request, Model model) {
        String loginUserId = SessionUtils.currentUserId(request);

        if (loginUserId == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("pageTitle", "대시보드");
        model.addAttribute("activeNav", "home");

        return "home/index";
    }
    
    @GetMapping("/error-test")
    public String errorTest() {
        throw new com.ssafy.yumyum.exception.CustomException(400, "커스텀 예외 처리 테스트입니다.");
    }
}