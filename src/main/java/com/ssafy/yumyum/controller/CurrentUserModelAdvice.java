package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.User;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserModelAdvice {

    @ModelAttribute("currentUser")
    public User currentUser(HttpServletRequest request) {
        Object currentUser = request.getAttribute("currentUser");
        return currentUser instanceof User user ? user : null;
    }
}
