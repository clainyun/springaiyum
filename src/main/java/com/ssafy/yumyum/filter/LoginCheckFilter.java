package com.ssafy.yumyum.filter;

import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginCheckFilter implements Filter {

    private final UserRepository userRepository;

    public LoginCheckFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        SessionUtils.exposeFlash(httpRequest);

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        String loginUserId = SessionUtils.currentUserId(httpRequest);

        if (loginUserId != null) {
            User currentUser = userRepository.findById(loginUserId);
            httpRequest.setAttribute("currentUser", currentUser);
        }

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (loginUserId == null) {
            SessionUtils.flash(httpRequest.getSession(true), "warning", "로그인이 필요합니다.");
            httpResponse.sendRedirect(contextPath + "/auth/login");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/")
                || path.equals("/auth/login")
                || path.equals("/auth/signup")
                || path.equals("/auth/logout")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/assets/")
                || path.startsWith("/webjars/")
                || path.equals("/favicon.ico")
                || path.equals("/error");
    }
}