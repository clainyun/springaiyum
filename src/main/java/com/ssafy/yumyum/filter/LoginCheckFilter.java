package com.ssafy.yumyum.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

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

@Component
public class LoginCheckFilter implements Filter {

    private static final String LOGIN_REQUIRED_MESSAGE = "\uB85C\uADF8\uC778\uC774 \uD544\uC694\uD55C \uBA54\uB274\uC785\uB2C8\uB2E4.";

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

        User currentUser = resolveCurrentUser(httpRequest);

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (currentUser == null) {
            handleUnauthorized(httpRequest, httpResponse, contextPath);
            return;
        }

        chain.doFilter(request, response);
    }

    private User resolveCurrentUser(HttpServletRequest request) {
        String loginUserId = SessionUtils.currentUserId(request);

        if (loginUserId == null) {
            return null;
        }

        User currentUser = userRepository.findById(loginUserId);
        if (currentUser == null || !currentUser.isActive()) {
            SessionUtils.logout(request.getSession(false));
            return null;
        }

        request.setAttribute("currentUser", currentUser);
        return currentUser;
    }

    private void handleUnauthorized(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String contextPath) throws IOException {

        if (expectsJson(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter()
                .write("{\"code\":\"AUTH_REQUIRED\",\"message\":\"" + LOGIN_REQUIRED_MESSAGE + "\"}");
            return;
        }

        SessionUtils.flash(request.getSession(true), "warning", LOGIN_REQUIRED_MESSAGE);
        response.sendRedirect(contextPath + "/auth/login");
    }

    private boolean expectsJson(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }

        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }

        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    private boolean isPublicPath(String path) {
        return path.equals("")
                || path.equals("/")
                || path.equals("/auth/login")
                || path.equals("/auth/signup")
                || path.equals("/auth/logout")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.equals("/v3/api-docs")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/assets/")
                || path.startsWith("/webjars/")
                || path.equals("/favicon.ico")
                || path.equals("/error");
    }
}
