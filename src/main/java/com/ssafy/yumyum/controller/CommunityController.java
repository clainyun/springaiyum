package com.ssafy.yumyum.controller;

import com.ssafy.yumyum.model.CommunityComment;
import com.ssafy.yumyum.model.CommunityPost;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.util.AppContainer;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;
import com.ssafy.yumyum.util.ViewHelper;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        Model model
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }
        return renderCommunityPage(user, resolveSelectedCategory(category, null), null, null, model);
    }

    @GetMapping("/posts/{postId}/edit")
    public String editPost(
        @ModelAttribute("currentUser") User user,
        @PathVariable String postId,
        @RequestParam(required = false) String category,
        HttpServletRequest req,
        Model model
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        CommunityPost post = AppContainer.getCommunityService().findPost(postId);
        if (post == null) {
            SessionUtils.flash(req.getSession(), "warning", "수정할 게시글이 없습니다.");
            return redirectToCommunity(resolveSelectedCategory(category, null));
        }
        if (!user.getId().equals(post.getUserId())) {
            SessionUtils.flash(req.getSession(), "warning", "본인 게시글만 수정할 수 있습니다.");
            return redirectToCommunity(resolveSelectedCategory(category, post.getCategory()));
        }
        return renderCommunityPage(user, resolveSelectedCategory(category, post.getCategory()), post, null, model);
    }

    @GetMapping("/comments/{commentId}/edit")
    public String editComment(
        @ModelAttribute("currentUser") User user,
        @PathVariable String commentId,
        @RequestParam(required = false) String category,
        HttpServletRequest req,
        Model model
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        CommunityComment comment = AppContainer.getCommunityService().findComment(commentId);
        if (comment == null) {
            SessionUtils.flash(req.getSession(), "warning", "수정할 댓글이 없습니다.");
            return redirectToCommunity(resolveSelectedCategory(category, null));
        }
        if (!user.getId().equals(comment.getUserId())) {
            SessionUtils.flash(req.getSession(), "warning", "본인 댓글만 수정할 수 있습니다.");
            return redirectToCommunity(resolveSelectedCategory(category, categoryForComment(comment)));
        }
        return renderCommunityPage(user, resolveSelectedCategory(category, categoryForComment(comment)), null, comment, model);
    }

    @PostMapping("/posts")
    public String createPost(
        @ModelAttribute("currentUser") User user,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String linkedMealId,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String content,
        HttpServletRequest req
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        ServiceResult<?> result = AppContainer.getCommunityService().createPost(user, category, linkedMealId, title, content);
        SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        return redirectToCommunity(resolveSelectedCategory(category, null));
    }

    @PatchMapping("/posts/{postId}")
    public String updatePost(
        @ModelAttribute("currentUser") User user,
        @PathVariable String postId,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String linkedMealId,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String content,
        HttpServletRequest req
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        ServiceResult<?> result = AppContainer.getCommunityService().updatePost(user, postId, category, linkedMealId, title, content);
        SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        return redirectToCommunity(resolveSelectedCategory(category, categoryForPostId(postId)));
    }

    @DeleteMapping("/posts/{postId}")
    public String deletePost(
        @ModelAttribute("currentUser") User user,
        @PathVariable String postId,
        @RequestParam(required = false) String redirectCategory,
        HttpServletRequest req
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        String fallbackCategory = categoryForPostId(postId);
        AppContainer.getCommunityService().deletePost(user, postId);
        SessionUtils.flash(req.getSession(), "success", "게시글을 삭제했습니다.");
        return redirectToCommunity(resolveSelectedCategory(redirectCategory, fallbackCategory));
    }

    @PostMapping("/posts/{postId}/comments")
    public String createComment(
        @ModelAttribute("currentUser") User user,
        @PathVariable String postId,
        @RequestParam(required = false) String commentContent,
        @RequestParam(required = false) String redirectCategory,
        HttpServletRequest req
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        ServiceResult<?> result = AppContainer.getCommunityService().addComment(user, postId, commentContent);
        SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        return redirectToCommunity(resolveSelectedCategory(redirectCategory, categoryForPostId(postId)));
    }

    @PatchMapping("/comments/{commentId}")
    public String updateComment(
        @ModelAttribute("currentUser") User user,
        @PathVariable String commentId,
        @RequestParam(required = false) String commentContent,
        @RequestParam(required = false) String redirectCategory,
        HttpServletRequest req
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        String fallbackCategory = categoryForCommentId(commentId);
        ServiceResult<?> result = AppContainer.getCommunityService().updateComment(user, commentId, commentContent);
        SessionUtils.flash(req.getSession(), result.isOk() ? "success" : "warning", result.getMessage());
        return redirectToCommunity(resolveSelectedCategory(redirectCategory, fallbackCategory));
    }

    @DeleteMapping("/comments/{commentId}")
    public String deleteComment(
        @ModelAttribute("currentUser") User user,
        @PathVariable String commentId,
        @RequestParam(required = false) String redirectCategory,
        HttpServletRequest req
    ) {
        if (user == null) {
            return LOGIN_REDIRECT;
        }

        String fallbackCategory = categoryForCommentId(commentId);
        AppContainer.getCommunityService().deleteComment(user, commentId);
        SessionUtils.flash(req.getSession(), "info", "댓글을 삭제했습니다.");
        return redirectToCommunity(resolveSelectedCategory(redirectCategory, fallbackCategory));
    }

    private String renderCommunityPage(
        User user,
        String selectedCategory,
        CommunityPost editPost,
        CommunityComment editComment,
        Model model
    ) {
        List<CommunityPost> posts = AppContainer.getCommunityService().getPosts(selectedCategory);
        Map<String, List<CommunityComment>> commentMap = AppContainer.getCommunityService().commentMap(posts);
        List<Meal> meals = AppContainer.getCommunityService().mealsForUser(user.getId());
        Map<String, User> authorMap = AppContainer.getCommunityService().authorMap(posts, commentMap);

        model.addAttribute("posts", posts);
        model.addAttribute("commentMap", commentMap);
        model.addAttribute("authorNameMap", authorNameMap(authorMap));
        model.addAttribute("meals", meals);
        model.addAttribute("categoryLabelMap", categoryLabelMap());
        model.addAttribute("mealLabelMap", mealLabelMap(meals));
        model.addAttribute("postCreatedAtMap", postCreatedAtMap(posts));
        model.addAttribute("commentCreatedAtMap", commentCreatedAtMap(commentMap));
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("editPost", editPost);
        model.addAttribute("editComment", editComment);
        return COMMUNITY_INDEX_VIEW;
    }

    private String redirectToCommunity(String category) {
        if (category == null || category.isBlank() || "all".equals(category)) {
            return COMMUNITY_REDIRECT;
        }
        return COMMUNITY_REDIRECT + "?category=" + category;
    }

    private String resolveSelectedCategory(String requestedCategory, String fallbackCategory) {
        if (requestedCategory != null && !requestedCategory.isBlank()) {
            return requestedCategory;
        }
        if (fallbackCategory != null && !fallbackCategory.isBlank()) {
            return fallbackCategory;
        }
        return "all";
    }

    private String categoryForPostId(String postId) {
        CommunityPost post = AppContainer.getCommunityService().findPost(postId);
        return post == null ? null : post.getCategory();
    }

    private String categoryForCommentId(String commentId) {
        CommunityComment comment = AppContainer.getCommunityService().findComment(commentId);
        return categoryForComment(comment);
    }

    private String categoryForComment(CommunityComment comment) {
        if (comment == null) {
            return null;
        }
        return categoryForPostId(comment.getPostId());
    }

    private Map<String, String> categoryLabelMap() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("review", ViewHelper.postCategoryLabel("review"));
        result.put("expert", ViewHelper.postCategoryLabel("expert"));
        result.put("free", ViewHelper.postCategoryLabel("free"));
        return result;
    }

    private Map<String, String> mealLabelMap(List<Meal> meals) {
        Map<String, String> result = new HashMap<>();
        for (Meal meal : meals) {
            result.put(
                meal.getId(),
                ViewHelper.formatDate(meal.getMealDate()) + " | " + ViewHelper.mealTypeLabel(meal.getMealType())
            );
        }
        return result;
    }

    private Map<String, String> postCreatedAtMap(List<CommunityPost> posts) {
        Map<String, String> result = new HashMap<>();
        for (CommunityPost post : posts) {
            result.put(post.getId(), ViewHelper.formatDateTime(post.getCreatedAt()));
        }
        return result;
    }

    private Map<String, String> commentCreatedAtMap(Map<String, List<CommunityComment>> commentMap) {
        Map<String, String> result = new HashMap<>();
        for (List<CommunityComment> comments : commentMap.values()) {
            for (CommunityComment comment : comments) {
                result.put(comment.getId(), ViewHelper.formatDateTime(comment.getCreatedAt()));
            }
        }
        return result;
    }

    private Map<String, String> authorNameMap(Map<String, User> authorMap) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, User> entry : authorMap.entrySet()) {
            User author = entry.getValue();
            result.put(entry.getKey(), author == null ? "알 수 없음" : author.getNickname());
        }
        return result;
    }
}
