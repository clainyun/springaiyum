package com.ssafy.yumyum.service;

import com.ssafy.yumyum.model.CommunityComment;
import com.ssafy.yumyum.model.CommunityPost;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.CommunityRepository;
import com.ssafy.yumyum.repository.MealRepository;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.util.IdGenerator;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SortUtils;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;

    public CommunityService(CommunityRepository communityRepository, UserRepository userRepository, MealRepository mealRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.mealRepository = mealRepository;
    }

    public List<CommunityPost> getPosts(String category) {
        List<CommunityPost> result = new ArrayList<>();
        for (CommunityPost post : communityRepository.findAllPosts()) {
            if (category == null || category.isEmpty() || "all".equals(category) || category.equals(post.getCategory())) {
                result.add(post);
            }
        }
        return SortUtils.quickSort(result, (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    }

    public CommunityPost findPost(String postId) {
        return communityRepository.findPostById(postId);
    }

    public CommunityComment findComment(String commentId) {
        return communityRepository.findCommentById(commentId);
    }

    public List<CommunityComment> commentsForPost(String postId) {
        List<CommunityComment> result = new ArrayList<>();
        for (CommunityComment comment : communityRepository.findAllComments()) {
            if (postId.equals(comment.getPostId())) {
                result.add(comment);
            }
        }
        return SortUtils.quickSort(result, (a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    }

    public Map<String, List<CommunityComment>> commentMap(List<CommunityPost> posts) {
        Map<String, List<CommunityComment>> map = new HashMap<>();
        for (CommunityPost post : posts) {
            map.put(post.getId(), commentsForPost(post.getId()));
        }
        return map;
    }

    public ServiceResult<CommunityPost> createPost(User user, String category, String linkedMealId, String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            return ServiceResult.failure("게시글 제목을 입력해 주세요.");
        }
        if (content == null || content.trim().isEmpty()) {
            return ServiceResult.failure("게시글 내용을 입력해 주세요.");
        }
        CommunityPost post = new CommunityPost();
        post.setId(IdGenerator.next("post"));
        post.setUserId(user.getId());
        post.setCategory(category);
        post.setLinkedMealId(linkedMealId == null || linkedMealId.trim().isEmpty() ? null : linkedMealId);
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        communityRepository.savePost(post);
        return ServiceResult.success("게시글을 등록했습니다.", post);
    }

    public ServiceResult<CommunityPost> updatePost(User user, String postId, String category, String linkedMealId, String title, String content) {
        CommunityPost post = communityRepository.findPostById(postId);
        if (post == null || !user.getId().equals(post.getUserId())) {
            return ServiceResult.failure("수정할 게시글이 없습니다.");
        }
        if (title == null || title.trim().isEmpty()) {
            return ServiceResult.failure("게시글 제목을 입력해 주세요.");
        }
        if (content == null || content.trim().isEmpty()) {
            return ServiceResult.failure("게시글 내용을 입력해 주세요.");
        }
        post.setCategory(category);
        post.setLinkedMealId(linkedMealId == null || linkedMealId.trim().isEmpty() ? null : linkedMealId);
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setUpdatedAt(LocalDateTime.now());
        communityRepository.savePost(post);
        return ServiceResult.success("게시글을 수정했습니다.", post);
    }

    public void deletePost(User user, String postId) {
        CommunityPost post = communityRepository.findPostById(postId);
        if (post != null && user.getId().equals(post.getUserId())) {
            communityRepository.deletePost(postId);
        }
    }

    public ServiceResult<CommunityComment> addComment(User user, String postId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return ServiceResult.failure("댓글 내용을 입력해 주세요.");
        }
        if (communityRepository.findPostById(postId) == null) {
            return ServiceResult.failure("댓글을 작성할 게시글이 없습니다.");
        }
        CommunityComment comment = new CommunityComment();
        comment.setId(IdGenerator.next("comment"));
        comment.setPostId(postId);
        comment.setUserId(user.getId());
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        communityRepository.saveComment(comment);
        return ServiceResult.success("댓글을 등록했습니다.", comment);
    }

    public ServiceResult<CommunityComment> updateComment(User user, String commentId, String content) {
        CommunityComment comment = communityRepository.findCommentById(commentId);
        if (comment == null || !user.getId().equals(comment.getUserId())) {
            return ServiceResult.failure("수정할 댓글이 없습니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            return ServiceResult.failure("댓글 내용을 입력해 주세요.");
        }
        comment.setContent(content.trim());
        comment.setUpdatedAt(LocalDateTime.now());
        communityRepository.saveComment(comment);
        return ServiceResult.success("댓글을 수정했습니다.", comment);
    }

    public void deleteComment(User user, String commentId) {
        CommunityComment comment = communityRepository.findCommentById(commentId);
        if (comment != null && user.getId().equals(comment.getUserId())) {
            communityRepository.deleteComment(commentId);
        }
    }

    public Map<String, User> authorMap(List<CommunityPost> posts, Map<String, List<CommunityComment>> commentMap) {
        Map<String, User> map = new HashMap<>();
        for (CommunityPost post : posts) {
            map.put(post.getUserId(), userRepository.findById(post.getUserId()));
        }
        for (List<CommunityComment> comments : commentMap.values()) {
            for (CommunityComment comment : comments) {
                map.put(comment.getUserId(), userRepository.findById(comment.getUserId()));
            }
        }
        return map;
    }

    public List<Meal> mealsForUser(String userId) {
        List<Meal> meals = new ArrayList<>();
        for (Meal meal : mealRepository.findAll()) {
            if (userId.equals(meal.getUserId())) {
                meals.add(meal);
            }
        }
        return SortUtils.quickSort(meals, (a, b) -> b.getMealDate().compareTo(a.getMealDate()));
    }
}
