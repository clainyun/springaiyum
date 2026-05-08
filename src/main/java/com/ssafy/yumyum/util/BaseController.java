package com.ssafy.yumyum.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.ssafy.yumyum.model.User;

public abstract class BaseController extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");
        SessionUtils.exposeFlash(req);
        super.service(req, resp);
    }

    protected void render(HttpServletRequest req, HttpServletResponse resp, String viewPath) throws ServletException, IOException {
        req.setAttribute("viewHelper", ViewHelper.class);
        req.getRequestDispatcher("/WEB-INF/views/" + viewPath + ".jsp").forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected User requireLoginUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AppContainer.getUserService().findById(SessionUtils.currentUserId(req));
        if (user == null || !user.isActive()) {
            HttpSessionHolder.flashLoginRequired(req);
            redirect(req, resp, "/auth/login");
            return null;
        }
        req.setAttribute("currentUser", user);
        return user;
    }

    private static final class HttpSessionHolder {
        private HttpSessionHolder() {
        }

        private static void flashLoginRequired(HttpServletRequest req) {
            SessionUtils.flash(req.getSession(), "warning", "로그인이 필요한 메뉴입니다.");
        }
    }
}
