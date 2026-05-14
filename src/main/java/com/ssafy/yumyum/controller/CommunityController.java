package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.CommunityComment;
import com.ssafy.yumyum.model.CommunityPost;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/community")
public class CommunityController {

    private static final String LOGIN_REDIRECT = "redirect:/auth/login";
    private static final String COMMUNITY_REDIRECT = "redirect:/community";
    private static final String COMMUNITY_INDEX_VIEW = "community/index";

    @ModelAttribute
    public void exposeFlash(HttpServletRequest req) {
        SessionUtils.exposeFlash(req);
    }

    @ModelAttribute("pageTitle")
    public String pageTitle() {
        return "커뮤니티";
    }

    @ModelAttribute("activeNav")
    public String activeNav() {
        return "community";
    }

    @ModelAttribute("currentUser")
    public User currentUser(
        @SessionAttribute(value = "loginUserId", required = false) String loginUserId,
        HttpServletRequest req
    ) {
        if (loginUserId == null) {
            SessionUtils.flash(req.getSession(), "warning", "로그인이 필요한 메뉴입니다.");
            return null;
        }

        User user = AppContainer.getUserService().findById(loginUserId);
        if (user == null || !user.isActive()) {
            SessionUtils.flash(req.getSession(), "warning", "로그인이 필요한 메뉴입니다.");
            return null;
        }
        return user;
    }

    @GetMapping
    public String getCommunity(
        @ModelAttribute("currentUser") User user,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String editPostId,
        @RequestParam(required = false) String editCommentId,
        Model model
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        List<CommunityPost> posts = AppContainer.getCommunityService().getPosts(category);
        Map<String, List<CommunityComment>> commentMap = AppContainer.getCommunityService().commentMap(posts);

        model.addAttribute("posts", posts);
        model.addAttribute("commentMap", commentMap);
        model.addAttribute("authorMap", AppContainer.getCommunityService().authorMap(posts, commentMap));
        model.addAttribute("meals", AppContainer.getCommunityService().mealsForUser(user.getId()));
        model.addAttribute("selectedCategory", category == null || category.isEmpty() ? "all" : category);
        model.addAttribute("editPost", AppContainer.getCommunityService().findPost(editPostId));
        model.addAttribute("editComment", AppContainer.getCommunityService().findComment(editCommentId));
        return COMMUNITY_INDEX_VIEW;
    }

    @PostMapping
    public String handleCommunityAction(
        @ModelAttribute("currentUser") User user,
        @RequestParam(required = false) String action,
        HttpServletRequest req
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        ServiceResult<?> result = null;
        if ("createPost".equals(action)) {
            result = AppContainer.getCommunityService().createPost(
                user,
                req.getParameter("category"),
                req.getParameter("linkedMealId"),
                req.getParameter("title"),
                req.getParameter("content")
            );
        } else if ("updatePost".equals(action)) {
            result = AppContainer.getCommunityService().updatePost(
                user,
                req.getParameter("postId"),
                req.getParameter("category"),
                req.getParameter("linkedMealId"),
                req.getParameter("title"),
                req.getParameter("content")
            );
        } else if ("deletePost".equals(action)) {
            AppContainer.getCommunityService().deletePost(user, req.getParameter("postId"));
            SessionUtils.flash(req.getSession(), "success", "게시글을 삭제했습니다.");
            return COMMUNITY_REDIRECT;
        } else if ("createComment".equals(action)) {
            result = AppContainer.getCommunityService().addComment(
                user,
                req.getParameter("postId"),
                req.getParameter("commentContent")
            );
        } else if ("updateComment".equals(action)) {
            result = AppContainer.getCommunityService().updateComment(
                user,
                req.getParameter("commentId"),
                req.getParameter("commentContent")
            );
        } else if ("deleteComment".equals(action)) {
            AppContainer.getCommunityService().deleteComment(user, req.getParameter("commentId"));
            SessionUtils.flash(req.getSession(), "info", "댓글을 삭제했습니다.");
            return COMMUNITY_REDIRECT;
        }

        if (result != null) {
            SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        }
        return COMMUNITY_REDIRECT;
    }
}
