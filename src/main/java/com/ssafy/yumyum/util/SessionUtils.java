package com.ssafy.yumyum.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUtils {

    private static final String LOGIN_USER_ID = "loginUserId";
    private static final String FLASH_MESSAGE = "flashMessage";
    private static final String FLASH_TYPE = "flashType";

    private SessionUtils() {
    }

    public static void login(HttpSession session, String userId) {
        session.setAttribute(LOGIN_USER_ID, userId);
    }

    public static String currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(LOGIN_USER_ID);
    }

    public static void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    public static void flash(HttpSession session, String type, String message) {
        session.setAttribute(FLASH_TYPE, type);
        session.setAttribute(FLASH_MESSAGE, message);
    }

    public static void exposeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object type = session.getAttribute(FLASH_TYPE);
        Object message = session.getAttribute(FLASH_MESSAGE);
        if (type != null) {
            request.setAttribute("flashType", type);
            session.removeAttribute(FLASH_TYPE);
        }
        if (message != null) {
            request.setAttribute("flashMessage", message);
            session.removeAttribute(FLASH_MESSAGE);
        }
    }
}
