package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.CommunityComment;
import com.ssafy.yumyum.model.CommunityPost;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.BaseController;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/community")
public class CommunityController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }

        String category = req.getParameter("category");
        List<CommunityPost> posts = AppContainer.getCommunityService().getPosts(category);
        Map<String, List<CommunityComment>> commentMap = AppContainer.getCommunityService().commentMap(posts);

        req.setAttribute("pageTitle", "커뮤니티");
        req.setAttribute("activeNav", "community");
        req.setAttribute("posts", posts);
        req.setAttribute("commentMap", commentMap);
        req.setAttribute("authorMap", AppContainer.getCommunityService().authorMap(posts, commentMap));
        req.setAttribute("meals", AppContainer.getCommunityService().mealsForUser(user.getId()));
        req.setAttribute("selectedCategory", category == null || category.isEmpty() ? "all" : category);
        req.setAttribute("editPost", AppContainer.getCommunityService().findPost(req.getParameter("editPostId")));
        req.setAttribute("editComment", AppContainer.getCommunityService().findComment(req.getParameter("editCommentId")));
        render(req, resp, "community/index");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireLoginUser(req, resp);
        if (user == null) {
            return;
        }

        String action = req.getParameter("action");
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
            redirect(req, resp, "/community");
            return;
        } else if ("createComment".equals(action)) {
            result = AppContainer.getCommunityService().addComment(user, req.getParameter("postId"), req.getParameter("commentContent"));
        } else if ("updateComment".equals(action)) {
            result = AppContainer.getCommunityService().updateComment(user, req.getParameter("commentId"), req.getParameter("commentContent"));
        } else if ("deleteComment".equals(action)) {
            AppContainer.getCommunityService().deleteComment(user, req.getParameter("commentId"));
            SessionUtils.flash(req.getSession(), "info", "댓글을 삭제했습니다.");
            redirect(req, resp, "/community");
            return;
        }

        if (result != null) {
            SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        }
        redirect(req, resp, "/community");
    }
}
