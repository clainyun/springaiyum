package com.ssafy.yumyum.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public String handleCustomException(CustomException e, Model model) {
        model.addAttribute("pageTitle", "오류");
        model.addAttribute("statusCode", e.getStatusCode());
        model.addAttribute("errorMessage", e.getMessage());

        return "error/error";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e, Model model) {
        model.addAttribute("pageTitle", "오류");
        model.addAttribute("statusCode", 500);
        model.addAttribute("errorMessage", e.getMessage());

        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("pageTitle", "오류");
        model.addAttribute("statusCode", 500);
        model.addAttribute("errorMessage", "처리 중 오류가 발생했습니다.");

        return "error/error";
    }
}