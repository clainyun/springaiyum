package com.ssafy.yumyum.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yumyum.dto.common.MessageResponse;
import com.ssafy.yumyum.dto.meal.MealSummaryResponse;
import com.ssafy.yumyum.exception.CustomException;
import com.ssafy.yumyum.model.CommunityComment;
import com.ssafy.yumyum.model.CommunityPost;
import com.ssafy.yumyum.model.Meal;
import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.service.CommunityService;
import com.ssafy.yumyum.service.MealService;
import com.ssafy.yumyum.util.ServiceResult;
import com.ssafy.yumyum.util.SessionUtils;
import com.ssafy.yumyum.util.ViewHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/community")
@Tag(name = "Community API", description = "커뮤니티 게시글/댓글 API")
public class CommunityApiController {

    private final CommunityService communityService;
    private final MealService mealService;
    private final UserRepository userRepository;

    public CommunityApiController(CommunityService communityService,
                                  MealService mealService,
                                  UserRepository userRepository) {
        this.communityService = communityService;
        this.mealService = mealService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "커뮤니티 목록 조회")
    public ResponseEntity<CommunityBoardResponse> list(@RequestParam(defaultValue = "all") String category,
                                                       HttpServletRequest request) {
        User user = getCurrentUser(request);
        List<CommunityPost> posts = communityService.getPosts(category);
        Map<String, List<CommunityComment>> commentMap = communityService.commentMap(posts);
        Map<String, User> authorMap = communityService.authorMap(posts, commentMap);
        List<MealSummaryResponse> meals = communityService.mealsForUser(user.getId()).stream()
                .map(meal -> MealSummaryResponse.from(meal, mealService.summarize(meal.getFoods())))
                .toList();

        return ResponseEntity.ok(new CommunityBoardResponse(
                resolveCategory(category),
                meals,
                posts.stream()
                        .map(post -> postResponse(post, commentMap.get(post.getId()), authorMap, user))
                        .toList()
        ));
    }

    @PostMapping("/posts")
    @Operation(summary = "게시글 작성")
    public ResponseEntity<MessageResponse> createPost(@RequestBody PostRequest request,
                                                      HttpServletRequest httpRequest) {
        User user = getCurrentUser(httpRequest);
        ServiceResult<?> result = communityService.createPost(
                user,
                request.category(),
                request.linkedMealId(),
                request.title(),
                request.content()
        );
        if (!result.isOk()) {
            throw new CustomException(400, result.getMessage());
        }
        return ResponseEntity.status(201).body(new MessageResponse(result.getMessage()));
    }

    @PatchMapping("/posts/{postId}")
    @Operation(summary = "게시글 수정")
    public ResponseEntity<MessageResponse> updatePost(@PathVariable String postId,
                                                      @RequestBody PostRequest request,
                                                      HttpServletRequest httpRequest) {
        User user = getCurrentUser(httpRequest);
        ServiceResult<?> result = communityService.updatePost(
                user,
                postId,
                request.category(),
                request.linkedMealId(),
                request.title(),
                request.content()
        );
        if (!result.isOk()) {
            throw new CustomException(400, result.getMessage());
        }
        return ResponseEntity.ok(new MessageResponse(result.getMessage()));
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "게시글 삭제")
    public ResponseEntity<MessageResponse> deletePost(@PathVariable String postId,
                                                      HttpServletRequest request) {
        communityService.deletePost(getCurrentUser(request), postId);
        return ResponseEntity.ok(new MessageResponse("게시글을 삭제했습니다."));
    }

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 작성")
    public ResponseEntity<MessageResponse> createComment(@PathVariable String postId,
                                                         @RequestBody CommentRequest request,
                                                         HttpServletRequest httpRequest) {
        ServiceResult<?> result = communityService.addComment(getCurrentUser(httpRequest), postId, request.content());
        if (!result.isOk()) {
            throw new CustomException(400, result.getMessage());
        }
        return ResponseEntity.status(201).body(new MessageResponse(result.getMessage()));
    }

    @PatchMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정")
    public ResponseEntity<MessageResponse> updateComment(@PathVariable String commentId,
                                                         @RequestBody CommentRequest request,
                                                         HttpServletRequest httpRequest) {
        ServiceResult<?> result = communityService.updateComment(getCurrentUser(httpRequest), commentId, request.content());
        if (!result.isOk()) {
            throw new CustomException(400, result.getMessage());
        }
        return ResponseEntity.ok(new MessageResponse(result.getMessage()));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable String commentId,
                                                        HttpServletRequest request) {
        communityService.deleteComment(getCurrentUser(request), commentId);
        return ResponseEntity.ok(new MessageResponse("댓글을 삭제했습니다."));
    }

    private CommunityPostResponse postResponse(CommunityPost post,
                                               List<CommunityComment> comments,
                                               Map<String, User> authorMap,
                                               User currentUser) {
        return new CommunityPostResponse(
                post.getId(),
                post.getUserId(),
                authorName(authorMap.get(post.getUserId())),
                post.getCategory(),
                ViewHelper.postCategoryLabel(post.getCategory()),
                post.getLinkedMealId(),
                post.getTitle(),
                post.getContent(),
                ViewHelper.formatDateTime(post.getCreatedAt()),
                currentUser.getId().equals(post.getUserId()),
                comments == null ? List.of() : comments.stream()
                        .map(comment -> commentResponse(comment, authorMap, currentUser))
                        .toList()
        );
    }

    private CommunityCommentResponse commentResponse(CommunityComment comment,
                                                     Map<String, User> authorMap,
                                                     User currentUser) {
        return new CommunityCommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getUserId(),
                authorName(authorMap.get(comment.getUserId())),
                comment.getContent(),
                ViewHelper.formatDateTime(comment.getCreatedAt()),
                currentUser.getId().equals(comment.getUserId())
        );
    }

    private String authorName(User user) {
        return user == null ? "알 수 없음" : user.getNickname();
    }

    private String resolveCategory(String category) {
        return category == null || category.isBlank() ? "all" : category;
    }

    private User getCurrentUser(HttpServletRequest request) {
        String loginUserId = SessionUtils.currentUserId(request);
        if (loginUserId == null) {
            throw new CustomException(401, "로그인이 필요합니다.");
        }

        User user = userRepository.findById(loginUserId);
        if (user == null || !user.isActive()) {
            throw new CustomException(401, "로그인 정보를 찾을 수 없습니다.");
        }
        return user;
    }

    public record CommunityBoardResponse(
            String selectedCategory,
            List<MealSummaryResponse> meals,
            List<CommunityPostResponse> posts
    ) {
    }

    public record CommunityPostResponse(
            String id,
            String userId,
            String authorName,
            String category,
            String categoryLabel,
            String linkedMealId,
            String title,
            String content,
            String createdAt,
            boolean canEdit,
            List<CommunityCommentResponse> comments
    ) {
    }

    public record CommunityCommentResponse(
            String id,
            String postId,
            String userId,
            String authorName,
            String content,
            String createdAt,
            boolean canEdit
    ) {
    }

    public record PostRequest(String category, String linkedMealId, String title, String content) {
    }

    public record CommentRequest(String content) {
    }
}
