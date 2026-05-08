package com.ssafy.yumyum.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ViewHelper {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private ViewHelper() {
    }

    public static String mealTypeLabel(String type) {
        if ("breakfast".equals(type)) {
            return "아침";
        }
        if ("lunch".equals(type)) {
            return "점심";
        }
        if ("dinner".equals(type)) {
            return "저녁";
        }
        if ("snack".equals(type)) {
            return "간식";
        }
        return "기타";
    }

    public static String goalLabel(String goal) {
        if ("health".equals(goal)) {
            return "건강 유지";
        }
        if ("diet".equals(goal)) {
            return "체중 감량";
        }
        if ("muscle".equals(goal)) {
            return "근육 증가";
        }
        return "사용자 목표";
    }

    public static String goalShortLabel(String goal) {
        if ("health".equals(goal)) {
            return "건강";
        }
        if ("diet".equals(goal)) {
            return "다이어트";
        }
        if ("muscle".equals(goal)) {
            return "근육";
        }
        return "목표";
    }

    public static String genderLabel(String gender) {
        return "female".equals(gender) ? "여성" : "남성";
    }

    public static String postCategoryLabel(String category) {
        if ("review".equals(category)) {
            return "식단 리뷰";
        }
        if ("expert".equals(category)) {
            return "전문가 팁";
        }
        if ("free".equals(category)) {
            return "자유 게시판";
        }
        return "커뮤니티";
    }

    public static String challengeStatusLabel(String status) {
        if ("completed".equals(status)) {
            return "완료";
        }
        if ("joined".equals(status)) {
            return "진행 중";
        }
        return "모집 중";
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DATE_FORMAT);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME_FORMAT);
    }

    public static int ageFromBirthYear(int birthYear) {
        return LocalDate.now().getYear() - birthYear + 1;
    }

    public static String nvl(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
