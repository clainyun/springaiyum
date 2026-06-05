package com.ssafy.yumyum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/home",
            "/auth/login",
            "/auth/signup",
            "/meals",
            "/meals/detail",
            "/meals/new",
            "/meals/edit",
            "/profile",
            "/coach",
            "/community",
            "/challenges",
            "/social"
    })
    public String spa() {
        return "forward:/index.html";
    }
}
